import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class BHSimulationGUI {
    private JFrame frame;
    private DefaultTableModel tableModel;
    private BHMain simulation;
    private DrawingPanel drawingPanel;
    private ArrayList<Body> planetArray;
    private final int HEIGHT = BHMain.HEIGHT;
    private final int WIDTH = BHMain.WIDTH;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BHSimulationGUI().createAndShowGUI());
    }

    public void createAndShowGUI() {
        frame = new JFrame("Barnes-Hut Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel controlPanel = createControlPanel();
        drawingPanel = new DrawingPanel();
        planetArray = new ArrayList<>();

        frame.getContentPane().add(controlPanel, BorderLayout.WEST);
        frame.getContentPane().add(drawingPanel, BorderLayout.EAST);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JButton runButton = new JButton("Run Simulation");
        runButton.addActionListener(e -> {
            frame.dispose();
            runSimulation();
        });

        // Create a panel for preset buttons
        JPanel presetPanel = new JPanel();

        int n = 5;
        // Add preset buttons
        for (int i = 1; i <= n; i++) {
            final int presetNumber = i;  // Make a copy to make it effectively final
            JButton presetButton = new JButton(String.valueOf(i));
            presetButton.addActionListener(e -> {
                initializePreset(presetNumber);
            });
            presetPanel.add(presetButton);
        }
        JButton clearPresetButton = new JButton("Clear body list");
        clearPresetButton.addActionListener(e -> {
            initializePreset(n + 1);
        });
        presetPanel.add(clearPresetButton);

        tableModel = new DefaultTableModel(new Object[]{"Mass", "Velocity X", "Velocity Y", "Position X", "Position Y", "Delete"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make all cells editable
                return true;
            }
        };

        tableModel.addTableModelListener(e -> {
            try {
                int row = e.getFirstRow();
                int column = e.getColumn();
                if (column == 5) {
                    deletePlanet(row);
                    drawingPanel.repaint();
                } else {
                    Object data = tableModel.getValueAt(row, column);
                    try {
                        double value = Double.parseDouble(data.toString());
                    } catch (NumberFormatException exception) {
                        JOptionPane.showMessageDialog(frame, "Invalid input. Please enter numeric values.");
                        tableModel.setValueAt(0.0, row, column);
                        return;
                    }
                    updateSimulationData(row, column, data);
                    drawingPanel.repaint();
                }
            } catch (ArrayIndexOutOfBoundsException exception) {
                // Do nothing--errors are from some stupid idiotic swing bug
            }
        });

        JTable planetTable = new JTable(tableModel);
        TableColumn deleteColumn = planetTable.getColumnModel().getColumn(5);
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> {
            int selectedRow = planetTable.getSelectedRow();
            if (selectedRow != -1) {
                deletePlanet(selectedRow);
            }
        });

        deleteColumn.setCellRenderer(new ButtonRenderer());
        deleteColumn.setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(planetTable);
        panel.add(runButton, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(presetPanel, BorderLayout.SOUTH); // Add the preset panel

        return panel;
    }


    // Add this method to initialize preset planetArrays
    private void initializePreset(int presetNumber) {
        // Clear existing data
        for (int i = planetArray.size() - 1; i >= 0; i--) {
            deletePlanet(i);
        }
        int n = 20;
        double m = 2.0;
        // Initialize empty planetArrays based on the preset number
        // For now, just initialize empty arrays
        switch (presetNumber) {
            case 1:
                double margin = 0.2;
                Random rand = new Random();
                for (int i = 0; i < n; i++) {
                    double x = WIDTH * (margin + (1 - 2 * margin) * rand.nextFloat());
                    double y = HEIGHT * (margin + (1 - 2 * margin) * rand.nextFloat());
                    double vx = 1 * (y - (double) HEIGHT / 2);
                    double vy = -1 * (x - (double) WIDTH / 2);
                    //if(rand.nextBoolean()){m*=-1;}
                    addPlanet(m, vx, vy, x, y);
                }
                break;
            case 2:
                double r = 0.4 * WIDTH;
                for (int i = 0; i < n; i++) {
                    double x = (double) WIDTH / 2 + r * Math.cos(2 * Math.PI * i / n);
                    double y = (double) HEIGHT / 2 + r * Math.sin(2 * Math.PI * i / n);
                    double vx = 1 * (y - HEIGHT / 2.0) * 0.5;
                    double vy = -1 * (x - WIDTH / 2.0) * 0.5;
                    vx = 0;
                    vy = 0;
                    addPlanet(m, vx, vy, x, y);
                }
                break;
            case 3:
                double spiralRadius = 0.4 * WIDTH;
                double rotationSpeed = 0.02;
                for (int i = 0; i < n; i++) {
                    double angle = rotationSpeed * i;
                    double x = (double) WIDTH / 2 + spiralRadius * angle * Math.cos(angle);
                    double y = (double) HEIGHT / 2 + spiralRadius * angle * Math.sin(angle);
                    double vx = 1 * (y - HEIGHT / 2.0) * 0.5;
                    double vy = -1 * (x - WIDTH / 2.0) * 0.5;
                    addPlanet(m, vx, vy, x, y);
                }
                break;
            case 4:
                int rows = 5;
                int cols = 5;
                double gridSpacing = 60;
                double angularSpeed = 0.03;

                int planetCount = 0;
                for (int i = 0; i < rows && planetCount < 25; i++) {
                    for (int j = 0; j < cols && planetCount < 25; j++) {
                        double x = (double) WIDTH / 2 + j * gridSpacing - (cols - 1) * gridSpacing / 2.0;
                        double y = (double) HEIGHT / 2 + i * gridSpacing - (rows - 1) * gridSpacing / 2.0;
                        double vx = 1 * (y - HEIGHT / 2.0) * 0.5;
                        double vy = -1 * (x - WIDTH / 2.0) * 0.5;
                        addPlanet(m, vx, vy, x, y);
                        planetCount++;
                    }
                }
                break;
            case 5:
                double cosmicRadius = 0.4 * WIDTH;
                double centralMass = 50;

                // Create a central mass with gravitational influence
                addPlanet(centralMass, 0, 0, WIDTH / 2, HEIGHT / 2);

                // Create orbiting planets forming a cosmic spiral
                int numPlanets = 10;
                double spiralFactor = 0.02;

                for (int i = 0; i < numPlanets; i++) {
                    double angle = 2 * Math.PI * i / numPlanets;
                    double radius = cosmicRadius + spiralFactor * angle;
                    double x = WIDTH / 2;
                    double y = HEIGHT / 2.0 - radius;
                    double vx = -1 * (y - HEIGHT / 2.0) * 4.5;
                    double vy = 0;
                    addPlanet(2, vx, vy, x, y);
                }
                break;
            default:
                // Do nothing

        }
        drawingPanel.repaint();
    }

    private void addPlanet(double mass, double velX, double velY, double posX, double posY) {
        Body newPlanet = new Body(posX, posY, velX, velY, mass);
        planetArray.add(newPlanet);
        tableModel.addRow(new Object[]{mass, velX, velY, posX, posY});
    }

    private void deletePlanet(int row) {
        planetArray.remove(row);
        tableModel.removeRow(row);
        drawingPanel.repaint();
    }

    private void runSimulation() {
        for (int i = planetArray.size() - 1; i > 0; i--) {
            if (planetArray.get(i).radius == 0) {
                planetArray.remove(i);
            } else {
                planetArray.get(i).yPos = BHMain.HEIGHT - planetArray.get(i).yPos;
            }
        }
        simulation = new BHMain(planetArray);
    }

    private void updateSimulationData(int row, int column, Object data) {
        try {
            double value = Double.parseDouble(data.toString());

            switch (column) {
                case 0:
                    // Mass column
                    planetArray.get(row).radius = value;
                    if(value < 0){throw new NumberFormatException();}
                    break;
                case 1:
                    // Velocity X column
                    planetArray.get(row).xVel = value;
                    break;
                case 2:
                    // Velocity Y column
                    planetArray.get(row).yVel = value;
                    break;
                case 3:
                    // Position X column
                    planetArray.get(row).xPos = value;
                    break;
                case 4:
                    // Position Y column
                    planetArray.get(row).yPos = value;
                    break;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid input. Please enter valid numeric values.");
        }
    }

    public static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText("Delete");
            return this;
        }
    }

    public static class ButtonEditor extends DefaultCellEditor {
        private final JButton button;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);

            // Add action listener to handle button clicks
            button.addActionListener(e -> {
                fireEditingStopped(); // Notify that editing has stopped
            });
            button.isFocusable();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            // Set text and return the button as the editor component
            button.setText("Delete");
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            // This method is called when editing is finished
            return "Delete";
        }
    }

    private class DrawingPanel extends JPanel {
        public DrawingPanel() {
            setPreferredSize(new Dimension(BHMain.WIDTH, BHMain.HEIGHT));
            setBackground(Color.BLACK);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    addPlanetAtMousePosition(e.getX(), e.getY());
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.WHITE);

            for (Body planet : planetArray) {
                planet.drawBody(g2d);
            }
        }

        private void addPlanetAtMousePosition(int x, int y) {
            // Prompt for mass and velocities
            String massString = JOptionPane.showInputDialog(frame, "Enter Mass:");
            String velXString = JOptionPane.showInputDialog(frame, "Enter Velocity X:");
            String velYString = JOptionPane.showInputDialog(frame, "Enter Velocity Y:");

            if (massString.isBlank()) massString = "0";
            if (velXString.isBlank()) velXString = "0";
            if (velYString.isBlank()) velYString = "0";

            try {
                double mass = Double.parseDouble(massString);
                if(mass < 0){throw new NumberFormatException();}

                double velX = Double.parseDouble(velXString);
                double velY = Double.parseDouble(velYString);
                addPlanet(mass, velX, velY, x, y);
                drawingPanel.repaint();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Invalid input. Please enter valid numeric values.");
            }
        }
    }
}
