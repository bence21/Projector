package projector.application;

import com.google.gson.annotations.Expose;

public class PdfViewerTabState {

    @Expose
    private String filePath;
    @Expose
    private int pageIndex = -1;
    @Expose
    private double scrollVvalue = -1;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public double getScrollVvalue() {
        return scrollVvalue;
    }

    public void setScrollVvalue(double scrollVvalue) {
        this.scrollVvalue = scrollVvalue;
    }
}
