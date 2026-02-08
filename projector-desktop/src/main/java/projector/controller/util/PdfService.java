package projector.controller.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.BufferUnderflowException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PdfService {

    /**
     * Constant representing that no page has been explicitly set
     */
    public static final int NO_PAGE_SET = -1;

    private static final Logger LOG = LoggerFactory.getLogger(PdfService.class);
    private static PdfService instance = null;
    private final Map<String, PDDocument> openDocuments = new HashMap<>();
    private final Map<String, Integer> currentPages = new HashMap<>();
    // Add locks for thread-safe rendering per document
    private final Map<String, Object> documentLocks = new ConcurrentHashMap<>();

    private PdfService() {
    }

    public static PdfService getInstance() {
        if (instance == null) {
            instance = new PdfService();
        }
        return instance;
    }

    public static boolean isPdfFile(String filePath) {
        if (filePath == null) {
            return false;
        }
        return filePath.toLowerCase().endsWith(".pdf");
    }

    public int getPageCount(String filePath) {
        try {
            PDDocument document = getDocument(filePath);
            if (document != null) {
                return document.getNumberOfPages();
            }
        } catch (Exception e) {
            LOG.error("Error getting page count for PDF: {}", filePath, e);
        }
        return 0;
    }

    public int getCurrentPage(String filePath) {
        return currentPages.getOrDefault(filePath, NO_PAGE_SET);
    }

    public boolean hasCurrentPage(String filePath) {
        return currentPages.containsKey(filePath);
    }

    public void setCurrentPage(String filePath, int page) {
        int pageCount = getPageCount(filePath);
        if (pageCount > 0) {
            int validPage = Math.max(0, Math.min(page, pageCount - 1));
            currentPages.put(filePath, validPage);
        }
    }

    public void nextPage(String filePath) {
        int currentPage = getCurrentPage(filePath);
        int pageCount = getPageCount(filePath);
        if (pageCount > 0 && currentPage < pageCount - 1) {
            setCurrentPage(filePath, currentPage + 1);
        }
    }

    public void previousPage(String filePath) {
        int currentPage = getCurrentPage(filePath);
        if (currentPage > 0) {
            setCurrentPage(filePath, currentPage - 1);
        }
    }

    public BufferedImage renderPage(String filePath, int pageIndex, float scale) {
        // Get or create a lock for this document to ensure thread-safe rendering
        Object lock = documentLocks.computeIfAbsent(filePath, k -> new Object());

        synchronized (lock) {
            try {
                PDDocument document = getDocument(filePath);
                if (document == null) {
                    return null;
                }

                int pageCount = document.getNumberOfPages();
                if (pageIndex < 0 || pageIndex >= pageCount) {
                    LOG.warn("Page index {} out of range for PDF with {} pages: {}", pageIndex, pageCount, filePath);
                    return null;
                }

                // Create a new PDFRenderer for each render call to avoid state issues
                PDFRenderer renderer = new PDFRenderer(document);
                // PDFBox may log MissingOperandException errors for malformed PDFs, but these are
                // typically non-fatal and rendering continues. The log4j configuration suppresses
                // these errors from PDFStreamEngine to reduce log noise.
                return renderer.renderImageWithDPI(pageIndex, scale);
            } catch (IllegalStateException e) {
                // Handle cases where page index is invalid or document state is corrupted
                LOG.warn("IllegalStateException rendering PDF page {}: {} - page may not exist or document state corrupted",
                        pageIndex, filePath, e);
                // Remove the document from cache so it gets reloaded on next access
                closeDocument(filePath);
                return null;
            } catch (java.io.IOException e) {
                // Handle cases where document is closed during rendering (e.g., during application shutdown or tab closing)
                if (e.getMessage() != null && e.getMessage().contains("already closed")) {
                    // Document was closed, likely during shutdown - this is expected, don't log as error
                    return null;
                }
                // Check for ClosedByInterruptException (happens when thread is interrupted during file I/O)
                if (e instanceof java.nio.channels.ClosedByInterruptException) {
                    // Thread was interrupted (e.g., tab closed while loading) - this is expected, don't log as error
                    return null;
                }
                // Other IO exceptions should be logged
                LOG.error("IOException rendering PDF page: {} page: {}", filePath, pageIndex, e);
                return null;
            } catch (BufferUnderflowException e) {
                // Handle corrupted/malformed PDF streams that cause buffer underflow
                // This can happen when PDF compression data is incomplete or corrupted
                LOG.warn("Buffer underflow while rendering PDF page (corrupted stream data): {} page: {}. " +
                        "The PDF may be corrupted or malformed.", filePath, pageIndex);
                return null;
            } catch (Exception e) {
                LOG.error("Error rendering PDF page: {} page: {}", filePath, pageIndex, e);
                return null;
            }
        }
    }

    public Image renderPageAsImage(String filePath, int pageIndex, float scale) {
        BufferedImage bufferedImage = renderPage(filePath, pageIndex, scale);
        if (bufferedImage != null) {
            return SwingFXUtils.toFXImage(bufferedImage, null);
        }
        return null;
    }

    public Image renderCurrentPageAsImage(String filePath, float scale) {
        int currentPage = getCurrentPageForRender(filePath);
        return renderPageAsImage(filePath, currentPage, scale);
    }

    private int getCurrentPageForRender(String filePath) {
        int currentPage = getCurrentPage(filePath);
        if (currentPage == NO_PAGE_SET) {
            return 0;
        }
        return currentPage;
    }

    private PDDocument getDocument(String filePath) {
        try {
            if (openDocuments.containsKey(filePath)) {
                PDDocument doc = openDocuments.get(filePath);
                if (doc != null) {
                    try {
                        // Check if document is still valid by accessing a property
                        doc.getNumberOfPages();
                        return doc;
                    } catch (Exception e) {
                        // Document is closed or invalid, remove it
                        openDocuments.remove(filePath);
                    }
                }
            }

            File file = new File(filePath);
            if (!file.exists()) {
                return null;
            }

            PDDocument document = Loader.loadPDF(file);
            openDocuments.put(filePath, document);
            if (!currentPages.containsKey(filePath)) {
                currentPages.put(filePath, NO_PAGE_SET);
            }
            return document;
        } catch (Exception e) {
            LOG.error("Error loading PDF document: {}", filePath, e);
            return null;
        }
    }

    public void closeDocument(String filePath) {
        try {
            PDDocument document = openDocuments.remove(filePath);
            if (document != null) {
                document.close();
            }
            currentPages.remove(filePath);
            // Remove the lock when document is closed
            documentLocks.remove(filePath);
        } catch (Exception e) {
            LOG.error("Error closing PDF document: {}", filePath, e);
        }
    }

    public void closeAllDocuments() {
        // Create a copy of file paths to avoid concurrent modification
        java.util.List<String> filePaths = new java.util.ArrayList<>(openDocuments.keySet());

        for (String filePath : filePaths) {
            closeDocument(filePath);
        }
    }

}

