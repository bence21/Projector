package com.bence.projector.common.converter;

import com.bence.projector.common.dto.SongDTO;
import com.bence.projector.common.dto.SongVerseDTO;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OpenLPXmlConverter {

    public static List<SongDTO> getXmlSongs(File[] files) {
        List<SongDTO> xmlSongs = new ArrayList<>(files.length);
        for (File file : files) {
            if (file.getName().endsWith(".xml")) {
                try {
                    xmlSongs.add(getSongFromOpenLpXmlFile(file));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return xmlSongs;
    }

    private static SongDTO getSongFromOpenLpXmlFile(File file) {
        SongDTO song = new SongDTO();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(file);
            document.getDocumentElement().normalize();
            NodeList list = document.getElementsByTagName("song");
            for (int i = 0; i < list.getLength(); i++) {
                org.w3c.dom.Node node = list.item(i);
                if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    setSongProperties(song, element.getElementsByTagName("properties"));
                    setSongLyrics(song, element.getElementsByTagName("lyrics"));
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return song;
    }

    private static void setSongLyrics(SongDTO song, NodeList lyrics) {
        ArrayList<SongVerseDTO> verses = new ArrayList<>();
        for (int i = 0; i < lyrics.getLength(); i++) {
            org.w3c.dom.Node node = lyrics.item(i);
            if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element element = (Element) node;
                verses.addAll(setSongVerses(element.getElementsByTagName("verse")));
            }
        }
        song.setSongVerseDTOS(verses);
    }

    private static List<SongVerseDTO> setSongVerses(NodeList verseList) {
        List<SongVerseDTO> songVerses = new ArrayList<>();
        for (int i = 0; i < verseList.getLength(); i++) {
            org.w3c.dom.Node node = verseList.item(i);
            if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element element = (Element) node;
                SongVerseDTO songVerse = new SongVerseDTO();
                songVerse.setText(setSongVerseByLines(element.getElementsByTagName("lines")));
                songVerses.add(songVerse);
            }
        }
        return songVerses;
    }

    private static String setSongVerseByLines(NodeList linesList) {
        StringBuilder lines = new StringBuilder();
        for (int i = 0; i < linesList.getLength(); i++) {
            org.w3c.dom.Node node = linesList.item(i);
            if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                if (lines.length() != 0) {
                    lines.append("\n");
                }
                Element element = (Element) node;
                org.w3c.dom.Node child = element.getFirstChild();
                while (child != null) {
                    lines.append(child.getTextContent().trim());
                    if (child.getNodeName().equals("br")) {
                        lines.append("\n");
                    }
                    child = child.getNextSibling();
                }
            }
        }
        return lines.toString();
    }

    private static void setSongProperties(SongDTO song, NodeList properties) {
        for (int i = 0; i < properties.getLength(); i++) {
            org.w3c.dom.Node node = properties.item(i);
            if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element element = (Element) node;
                setSongTitles(song, element.getElementsByTagName("titles"));
                setSongAuthors(song, element.getElementsByTagName("authors"));
            }
        }
    }

    private static void setSongAuthors(SongDTO song, NodeList authors) {
        for (int i = 0; i < authors.getLength(); i++) {
            org.w3c.dom.Node node = authors.item(i);
            if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element element = (Element) node;
                NodeList author = element.getElementsByTagName("author");
                setSongAuthor(song, author);
            }
        }
    }

    private static void setSongAuthor(SongDTO song, NodeList authorList) {
        StringBuilder authorText = new StringBuilder();
        for (int i = 0; i < authorList.getLength(); i++) {
            org.w3c.dom.Node node = authorList.item(i);
            if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (!authorText.toString().equals("")) {
                    authorText.append(", ");
                }
                authorText.append(element.getTextContent().trim());
            }
        }
        song.setAuthor(authorText.toString());
    }

    private static void setSongTitles(SongDTO song, NodeList titles) {
        for (int i = 0; i < titles.getLength(); i++) {
            org.w3c.dom.Node node = titles.item(i);
            if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element element = (Element) node;
                NodeList title = element.getElementsByTagName("title");
                setSongTitle(song, title);
            }
        }
    }

    private static void setSongTitle(SongDTO song, NodeList title) {
        for (int i = 0; i < title.getLength(); i++) {
            org.w3c.dom.Node node = title.item(i);
            if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element element = (Element) node;
                song.setTitle(element.getTextContent().trim());
                break;
            }
        }
    }
}
