/*
 * Copyright (C) 1999-2019 David Schweinsberg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sourceforge.cssparser.browser;

import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSStyleSheet;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;

/**
 * @author David Schweinsberg
 */
public class StyleBrowser {

    private final JFrame frame_;
    private final JTree tree_;
    private DefaultTreeModel treeModel_;
    private boolean macPlatform_;

    public StyleBrowser() {

        if (System.getProperty("os.name").equals("Mac OS X")) {
            macPlatform_ = true;

            // Before loading Swing, set macOS-specific properties
            System.setProperty("apple.awt.application.name", "Style Browser");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        } else {
            macPlatform_ = false;
        }

        frame_ = new JFrame("Style Browser");

        JPanel panel = new JPanel(true);
        frame_.getContentPane().add("Center", panel);

        frame_.setJMenuBar(createMenuBar());

        tree_ = new JTree((TreeModel) null);

        // Enable tool tips for the tree, without this tool tips will not
        // be picked up
        ToolTipManager.sharedInstance().registerComponent(tree_);

        // Make the tree use an instance of StyleBrowserCellRenderer for
        // drawing
        tree_.setCellRenderer(new StyleBrowserCellRenderer());

        // Put the Tree in a scroller
        JScrollPane sp = new JScrollPane();
        sp.setPreferredSize(new Dimension(500, 500));
        sp.getViewport().add(tree_);

        // Show it
        panel.setLayout(new BorderLayout());
        panel.add("Center", sp);

        frame_.addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                }
        );

        frame_.pack();
        frame_.show();
    }

    private JMenuBar createMenuBar() {
        if (macPlatform_) {
            registerForMacOSXEvents();
        }
        JMenu menu;
        JMenuBar menuBar = new JMenuBar();
        JMenuItem menuItem;

        menu = new JMenu("File");
        menuBar.add(menu);

        menuItem = menu.add(new JMenuItem("Open..."));
        menuItem.addActionListener(e -> openStyleSheet());

        if (!macPlatform_) {
            menuItem = menu.add(new JMenuItem("Exit"));
            menuItem.addActionListener(e -> System.exit(0));
        }

        menu = new JMenu("Help");
        menuBar.add(menu);

        if (!macPlatform_) {
            menuItem = menu.add(new JMenuItem("About"));
            menuItem.addActionListener(e -> showAbout());
        }

        return menuBar;
    }

    private void loadStyleSheet(String pathName) {
        try {
            frame_.setCursor(new Cursor(Cursor.WAIT_CURSOR));

            // Parse the style sheet
            Reader r = new FileReader(pathName);
            InputSource is = new InputSource(r);
            CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
            CSSStyleSheet stylesheet = parser.parseStyleSheet(is, null, null);

            // Create the tree to put the information in
            treeModel_ = (DefaultTreeModel) StyleBrowserTreeBuilder.createStyleSheetTree(stylesheet);

            tree_.setModel(treeModel_);

            frame_.setTitle("Style Browser - " + pathName);
        } catch (CSSException e) {
            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage(),
                    "CSS Exception",
                    JOptionPane.ERROR_MESSAGE);
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage(),
                    "File Not Found",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage(),
                    "I/O Exception",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            frame_.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private void openStyleSheet() {
        JFileChooser chooser = new JFileChooser();

        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getPath().endsWith(".css");
            }

            @Override
            public String getDescription() {
                return "Cascading Style Sheets";
            }
        };
        chooser.setFileFilter(filter);

        if (chooser.showOpenDialog(frame_) == JFileChooser.APPROVE_OPTION) {
            loadStyleSheet(chooser.getSelectedFile().getPath());
        }
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(
                null,
                "DOM CSS Style Browser\n"
                + "Copyright © 1999-2019 David Schweinsberg\n"
                + "github.com/cssparser",
                "About Style Browser",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Generic registration with the macOS application menu
    private void registerForMacOSXEvents() {
        Desktop desktop = Desktop.getDesktop();
        desktop.setQuitHandler((e, response) -> System.exit(0));
        desktop.setAboutHandler(e -> showAbout());
    }

    static public void main(String[] args) {
        new StyleBrowser();
    }
}
