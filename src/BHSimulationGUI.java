import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class BHSimulationGUI {
    private JFrame frame;
    private DefaultTableModel tableModel;
    private BHMain simulation;
    private DrawingPanel drawingPanel;
    private ArrayList<Body> planetArray;

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
        // Add a button column for deletion
        TableColumn deleteColumn = planetTable.getColumnModel().getColumn(5);
        //Chatgpt: add delete butons to the fifth column
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

        return panel;
    }

    private void deletePlanet(int row) {
        planetArray.remove(row);
        tableModel.removeRow(row);
        drawingPanel.repaint();
    }

    private void runSimulation() {
        for (int i = planetArray.size()-1; i > 0; i--) {
            if (planetArray.get(i).getRadius() == 0) {
                planetArray.remove(i);
            } else {
                planetArray.get(i).setY(BHMain.HEIGHT - planetArray.get(i).getY());
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
                    planetArray.get(row).setRadius(value);
                    break;
                case 1:
                    // Velocity X column
                    planetArray.get(row).setVelocityX(value);
                    break;
                case 2:
                    // Velocity Y column
                    planetArray.get(row).setVelocityY(value);
                    break;
                case 3:
                    // Position X column
                    planetArray.get(row).setX(value);
                    break;
                case 4:
                    // Position Y column
                    planetArray.get(row).setY(value);
                    break;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid input. Please enter numeric values.");
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
                double velX = Double.parseDouble(velXString);
                double velY = Double.parseDouble(velYString);

                Body newPlanet = new Body(x, y, velX, velY, mass);
                planetArray.add(newPlanet);
                tableModel.addRow(new Object[]{mass, velX, velY, x, y});
                drawingPanel.repaint();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Invalid input. Please enter numeric values.");
            }
        }
    }
}
