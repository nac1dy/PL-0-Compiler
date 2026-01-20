package Compiler;

import TestPrinter.ASTPrinter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;

class GUI extends JFrame implements ActionListener {

    String source;
    static JPanel topPanel;
    static JButton compile;
    static JPanel centerPanel;
    static JPanel leftPanel;
    static JTextArea leftTopTA;
    static JTextArea leftBottomTA;
    static JFrame frame;
    static JLabel leftLabel;
    static JLabel CodeLabel;
    static JLabel OutputLabel;
    static JLabel ErrorLabel;
    static JPanel middlePanel;
    static JTextArea middleTopTA;
    static JTextArea middleBottomTA;
    static JTabbedPane rightTabs;
    static JTextArea rightTA1;
    static JTextArea rightTA2;
    static JTextArea rightTA3;

    static ProcessBuilder pb;
    static String command = """
            if [[ -x "compiled/r2432" ]]; then
              (cd "compiled" && ./r2432 out.cl0)
            else
              echo "WARN: compiled/r2432 not found or not executable; skipping execution." >&2
            fi
            """;

    // pro Compile-Lauf: wurde etwas nach System.err geschrieben?
    private static volatile boolean hasCompilerErrors = false;

    public GUI()
    {
    }

    public static void main(String[] args) {
        pb = new ProcessBuilder("/bin/bash", "-c", command);

        pb.redirectErrorStream(true);
        frame = new JFrame("GUI PL/0 COMPILER");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 1000);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // ---------- TOP (Button zentral oben) ----------
        topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        compile = new JButton("COMPILE!");
        compile.addActionListener(new GUI());
        topPanel.add(compile);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // ---------- CENTER (3 Spalten: Links | Mitte | Rechts) ----------
        centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        // ========== LINKS ==========
        leftPanel = new JPanel(new GridBagLayout());
        GridBagConstraints lbc = new GridBagConstraints();
        lbc.fill = GridBagConstraints.BOTH;
        lbc.insets = new Insets(5, 5, 5, 5);
        lbc.weightx = 1;

        CodeLabel = new JLabel("Enter PL/0 Code!");
        leftTopTA = new JTextArea();
        leftBottomTA = new JTextArea(5,10);
        leftLabel = new JLabel("Input if needed");

        // Spalte 0: Label über TextArea, dann Label über Input-TextArea
        lbc.gridx = 0;

        lbc.gridy = 0;
        lbc.weighty = 0;
        leftPanel.add(CodeLabel, lbc);

        lbc.gridy = 1;
        lbc.weighty = 1;
        JScrollPane leftTopScroll = new JScrollPane(leftTopTA);
        leftTopScroll.setRowHeaderView(new LineNumberView(leftTopTA));
        leftPanel.add(leftTopScroll, lbc);

        lbc.gridy = 2;
        lbc.weighty = 0;
        leftPanel.add(leftLabel, lbc);

        lbc.gridy = 3;
        lbc.weighty = 0.4;
        leftPanel.add(new JScrollPane(leftBottomTA), lbc);


        // ========== MITTE ==========
        middlePanel = new JPanel(new GridBagLayout());
        GridBagConstraints mbc = new GridBagConstraints();
        mbc.fill = GridBagConstraints.BOTH;
        mbc.insets = new Insets(5, 5, 5, 5);
        mbc.weightx = 1;

        OutputLabel = new JLabel("Output from PL/0 Code");
        middleTopTA = new JTextArea();
        ErrorLabel = new JLabel("Error-Messages");
        middleBottomTA = new JTextArea();

        mbc.gridx = 0;

        mbc.gridy = 0;
        mbc.weighty = 0;
        middlePanel.add(OutputLabel, mbc);

        mbc.gridy = 1;
        mbc.weighty = 0.6;
        middlePanel.add(new JScrollPane(middleTopTA), mbc);

        mbc.gridy = 2;
        mbc.weighty = 0;
        middlePanel.add(ErrorLabel, mbc);

        mbc.gridy = 3;
        mbc.weighty = 0.4;
        middlePanel.add(new JScrollPane(middleBottomTA), mbc);

        // ========== RECHTS ==========
        rightTabs = new JTabbedPane();

        rightTA1 = new JTextArea();
        rightTA2 = new JTextArea();
        rightTA3 = new JTextArea();

        rightTabs.addTab("AST", new JScrollPane(rightTA1));
        rightTabs.addTab("DataBunker-Infos", new JScrollPane(rightTA2));
        rightTabs.addTab("ByteCode", new JScrollPane(rightTA3));

        // ---------- CENTER zusammensetzen ----------
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        gbc.weighty = 1;
        centerPanel.add(leftPanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.4;
        centerPanel.add(middlePanel, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.3;
        centerPanel.add(rightTabs, gbc);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // ---------- FRAME ----------
        frame.setContentPane(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Java-Compiler-Ausgaben in die TextAreas umleiten (wichtig: nach Erstellung der TextAreas)
        redirectSystemStreams();
    }

    private static void redirectSystemStreams() {
        // System.err -> Error-Box
        System.setErr(new PrintStream(new TextAreaOutputStream(middleBottomTA, true), true, StandardCharsets.UTF_8));
        // System.out NICHT umleiten: Output oben soll nur das Program-Ergebnis (r2432) zeigen.
    }

    private static final class TextAreaOutputStream extends OutputStream {
        private final JTextArea target;
        private final boolean marksError;

        private TextAreaOutputStream(JTextArea target, boolean marksError) {
            this.target = target;
            this.marksError = marksError;
        }

        @Override
        public void write(int b) {
            if (marksError) hasCompilerErrors = true;
            append(String.valueOf((char) b));
        }

        @Override
        public void write(byte[] b, int off, int len) {
            if (b == null || len <= 0) return;
            if (marksError) hasCompilerErrors = true;
            append(new String(b, off, len, StandardCharsets.UTF_8));
        }

        private void append(String text) {
            if (text == null || text.isEmpty()) return;
            SwingUtilities.invokeLater(() -> {
                target.append(text);
                target.setCaretPosition(target.getDocument().getLength());
            });
        }
    }

    /**
     * Sehr leichte Zeilennummern-Ansicht für eine JTextArea.
     * Wird als RowHeaderView der JScrollPane genutzt.
     */
    private static final class LineNumberView extends JComponent implements DocumentListener {
        private static final int PADDING = 5;

        private final JTextArea textArea;
        private final FontMetrics fontMetrics;

        private LineNumberView(JTextArea textArea) {
            this.textArea = textArea;
            this.fontMetrics = textArea.getFontMetrics(textArea.getFont());

            setFont(textArea.getFont());
            setForeground(Color.GRAY);
            setBackground(new Color(245, 245, 245));
            setOpaque(true);

            textArea.getDocument().addDocumentListener(this);
        }

        @Override
        public Dimension getPreferredSize() {
            int lineCount = Math.max(1, textArea.getLineCount());
            int digits = String.valueOf(lineCount).length();
            int width = PADDING * 2 + fontMetrics.charWidth('0') * digits;
            return new Dimension(width, textArea.getHeight());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Rectangle clip = g.getClipBounds();
            g.setColor(getBackground());
            g.fillRect(clip.x, clip.y, clip.width, clip.height);

            g.setColor(getForeground());

            int startOffset = textArea.viewToModel2D(new Point(0, clip.y));
            int endOffset = textArea.viewToModel2D(new Point(0, clip.y + clip.height));

            try {
                int startLine = textArea.getLineOfOffset(startOffset);
                int endLine = textArea.getLineOfOffset(endOffset);

                for (int line = startLine; line <= endLine; line++) {
                    int lineStart = textArea.getLineStartOffset(line);
                    Rectangle r = textArea.modelToView2D(lineStart).getBounds();

                    String number = String.valueOf(line + 1);
                    int x = getPreferredSize().width - PADDING - fontMetrics.stringWidth(number);
                    int y = r.y + r.height - fontMetrics.getDescent();
                    g.drawString(number, x, y);
                }
            } catch (Exception ignored) {
                // Bei Race-Conditions während Edit/Scroll einfach nichts zeichnen.
            }
        }

        private void refresh() {
            revalidate();
            repaint();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            refresh();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            refresh();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            refresh();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Vorherige Ausgaben leeren
        middleTopTA.setText("");
        middleBottomTA.setText("");
        hasCompilerErrors = false;

        source = leftTopTA.getText();
        System.out.println("Compiling " + source);

        if (Compiler.containsInput(source)) {
            leftBottomTA.setVisible(true);
        } else {
            leftBottomTA.setVisible(false);
            leftBottomTA.setText("");
        }

        // Compiler laufen lassen: schreibt Fehler nach System.err -> Error-Box + setzt hasCompilerErrors
        Compiler.run(source);

        // Wenn es Compiler-Fehler gibt: kein r2432-Lauf und Output bleibt leer (aber sichtbar)
        if (hasCompilerErrors) {
            return;
        }

        try {
            Process process = pb.start();

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                if (leftBottomTA.isVisible() && leftBottomTA.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter the code to compile");
                } else {
                    writer.write(leftBottomTA.getText());
                    writer.flush();
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // r2432-Ausgabe in die Output-Box
                    middleTopTA.append(line + "\n");
                    middleTopTA.setEditable(false);
                    rightTA1.setText(new ASTPrinter().print(Compiler.getProgram()));
                    rightTA2.setText(Compiler.getBunkerOutput());
                    rightTA3.setText(Compiler.getBytecode_String());
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            process.waitFor();
        } catch (InterruptedException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
