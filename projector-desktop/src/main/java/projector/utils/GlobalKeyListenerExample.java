package projector.utils;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import projector.application.ProjectionType;
import projector.controller.ProjectionScreenController;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GlobalKeyListenerExample implements NativeKeyListener {

    private ProjectionScreenController projectionScreenController;
    private boolean controlPressed = false;
    private boolean metaPressed = false;

    public void nativeKeyPressed(NativeKeyEvent e) {
        int keyCode = e.getKeyCode();
        System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(keyCode));
        if (keyCode == NativeKeyEvent.VC_CONTROL) {
            controlPressed = true;
        }
        if (keyCode == NativeKeyEvent.VC_META) {
            metaPressed = true;
        }
        //if (keyCode == NativeKeyEvent.VC_ESCAPE) {
        //    try {
        //        GlobalScreen.unregisterNativeHook();
        //    } catch (NativeHookException e1) {
        //        e1.printStackTrace();
        //    }
        //}
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        int keyCode = e.getKeyCode();
        if (controlPressed && keyCode == NativeKeyEvent.VC_C) {
            try {
                Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                List<DataFlavor> dataFlavorList = new ArrayList<>();
                dataFlavorList.add(DataFlavor.stringFlavor);
                dataFlavorList.add(DataFlavor.allHtmlFlavor);
                dataFlavorList.add(DataFlavor.selectionHtmlFlavor);
                dataFlavorList.add(DataFlavor.fragmentHtmlFlavor);
                dataFlavorList.add(DataFlavor.imageFlavor);
                dataFlavorList.add(DataFlavor.javaFileListFlavor);
                for (DataFlavor dataFlavor : dataFlavorList) {
                    if (systemClipboard.isDataFlavorAvailable(dataFlavor)) {
                        String clipBoardText = (String) systemClipboard.getData(dataFlavor);
                        projectionScreenController.setText(clipBoardText, ProjectionType.CLIP_BOARD, null);
                    }
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                ex.printStackTrace();
            }
        }
        if (metaPressed && keyCode == NativeKeyEvent.VC_LEFT) {
            System.out.println("TO Left");
        }
        if (keyCode == NativeKeyEvent.VC_CONTROL) {
            controlPressed = false;
        }
        if (keyCode == NativeKeyEvent.VC_META) {
            metaPressed = false;
        }
        System.out.println("Key Released: " + NativeKeyEvent.getKeyText(keyCode));
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
        int keyCode = e.getKeyCode();
        System.out.println("Key Typed: " + NativeKeyEvent.getKeyText(keyCode));
    }

    public void setProjectionScreenController(ProjectionScreenController projectionScreenController) {
        this.projectionScreenController = projectionScreenController;
    }
}
