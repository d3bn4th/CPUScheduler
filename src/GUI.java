
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class GUI
{
    // Initializing different components of the GUI 
    private JFrame frame;
    private JPanel mainPanel;
    private CustomPanel chartPanel;
    private JScrollPane tablePane;
    private JScrollPane chartPane;
    private JTable table;
    private JButton addBtn;
    private JButton removeBtn;
    private JButton computeBtn;
    private JLabel wtLabel;
    private JLabel wtResultLabel;
    private JLabel tatLabel;
    private JLabel tatResultLabel;
    private JComboBox option;
    private DefaultTableModel model;
    
    // Constructor to initialize  the components of the GUI(JTable, JButton, JLabel, and JComboBox objects, and sets their properties.)
    public GUI()
    {
        // sets the column names and the row count(initially 0)
        model = new DefaultTableModel(new String[]{"Process", "AT", "BT", "Priority", "WT", "TAT"}, 0); 
        
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        tablePane = new JScrollPane(table);
        tablePane.setBounds(25, 25, 450, 250);
        // Add Button : to an empty row for process description
        addBtn = new JButton("Add");
        addBtn.setBounds(300, 280, 85, 25);
        addBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        addBtn.addActionListener(
            new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                model.addRow(new String[]{"", "", "", "", "", ""}); // adds an empty row
            } 
        });

        // Remove Button : to remove a selected row
        removeBtn = new JButton("Remove");
        removeBtn.setBounds(390, 280, 85, 25);
        removeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        removeBtn.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                
                if (row > -1) {
                    model.removeRow(row);
                }
            }
        });
        
        // * Panel to display the Gantt Chart
        chartPanel = new CustomPanel();
        //chartPanel.setPreferredSize(new Dimension(700, 10));
        chartPanel.setBackground(Color.lightGray);
        chartPane = new JScrollPane(chartPanel);
        chartPane.setBounds(25, 310, 450, 100); // positions the object in the panel
        
        // * Panel for AWT and TAT Results
        wtLabel = new JLabel("Average Waiting Time:");
        wtLabel.setBounds(25, 425, 180, 25);
        // void java.awt.Component.setBounds(int x, int y, int width, int height)
        
        tatLabel = new JLabel("Average Turn Around Time:");
        tatLabel.setBounds(25, 450, 180, 25);

        wtResultLabel = new JLabel();
        wtResultLabel.setBounds(215, 425, 180, 25);
        
        tatResultLabel = new JLabel();
        tatResultLabel.setBounds(215, 450, 180, 25);
        
        // * drop-down menu in the  GUI that allows the user to select a scheduling algorithm.
        //  The available algorithms are "FCFS" (First-Come, First-Served), "SJF" (Shortest Job First), "SRT" (Shortest Remaining Time), "PSN" (Priority Scheduling - Non-preemptive), "PSP" (Priority Scheduling - Preemptive), and "RR" (Round Robin).
        option = new JComboBox(new String[]{"FCFS", "SJF", "SRT", "PSN", "PSP", "RR"});
        option.setBounds(390, 420, 85, 20);
        
        // Compute Button
        computeBtn = new JButton("Compute");
        computeBtn.setBounds(390, 450, 85, 25);
        computeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        computeBtn.addActionListener(new ActionListener(){ // what happens when clicked
            @Override
            public void actionPerformed(ActionEvent e) {
                String selected = (String) option.getSelectedItem();
                CPUScheduler scheduler;
                
                switch (selected) {
                    case "FCFS":
                        scheduler = new FirstComeFirstServe();
                        break;
                    case "SJF":
                        scheduler = new ShortestJobFirst();
                        break;
                    case "SRT":
                        scheduler = new ShortestRemainingTime();
                        break;
                    case "PSN":
                        scheduler = new PriorityNonPreemptive();
                        break;
                    case "PSP":
                        scheduler = new PriorityPreemptive();
                        break;
                    case "RR":
                        // When Round Robin is selected it prompts the  user to enter the quantum time needed for the algorithm
                        String tq = JOptionPane.showInputDialog("Time Quantum");
                        if (tq == null) {
                            return;
                        }
                        scheduler = new RoundRobin();
                        scheduler.setTimeQuantum(Integer.parseInt(tq));//passes the QT to the object
                        break;
                    default:
                        return;
                }
                
                for (int i = 0; i < model.getRowCount(); i++)
                {
                    String process = (String) model.getValueAt(i, 0);
                    int at = Integer.parseInt((String) model.getValueAt(i, 1));
                    int bt = Integer.parseInt((String) model.getValueAt(i, 2));
                    int pl;
                    
                    if (selected.equals("PSN") || selected.equals("PSP"))
                    {
                        if (!model.getValueAt(i, 3).equals(""))
                        {
                            pl = Integer.parseInt((String) model.getValueAt(i, 3));
                        }
                        else
                        {
                            pl = 1;
                        }
                    }
                    else
                    {
                        pl = 1;
                    }
                                        
                    scheduler.add(new Row(process, at, bt, pl));
                }
                
                scheduler.process();
                
                for (int i = 0; i < model.getRowCount(); i++)
                {
                    String process = (String) model.getValueAt(i, 0);
                    Row row = scheduler.getRow(process);
                    model.setValueAt(row.getWaitingTime(), i, 4);
                    model.setValueAt(row.getTurnaroundTime(), i, 5);
                }
                
                wtResultLabel.setText(Double.toString(scheduler.getAverageWaitingTime()));
                tatResultLabel.setText(Double.toString(scheduler.getAverageTurnAroundTime()));
                
                chartPanel.setTimeline(scheduler.getTimeline());
            }
        });
        
        // adds all the components to the mainPanel
        mainPanel = new JPanel(null);
        mainPanel.setPreferredSize(new Dimension(500, 500)); // setting the dimensions
        mainPanel.add(tablePane);
        mainPanel.add(addBtn);
        mainPanel.add(removeBtn);
        mainPanel.add(chartPane);
        mainPanel.add(wtLabel);
        mainPanel.add(tatLabel);
        mainPanel.add(wtResultLabel);
        mainPanel.add(tatResultLabel);
        mainPanel.add(option);
        mainPanel.add(computeBtn);
        
        frame = new JFrame("CPU Scheduler Simulator - by Rahul(09), Arya(059) & Arihant(033)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.add(mainPanel);
        frame.pack();
    }
    
    // * Main Method
    public static void main(String[] args)
    {
        new GUI(); // initializes the components of the GUI and displays on the Screen  
    }
    
    // to draw/update a timeline of events on the panel based on a given list of Event objects.
    // helps drawing the Gantt Chart 
    // the CustomPanel class provides a visual representation of the timeline of events
    class CustomPanel extends JPanel
    {   
        private List<Event> timeline;
        
        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            
            if (timeline != null)
            {
                // int width = 30;
                
                for (int i = 0; i < timeline.size(); i++)
                {
                    Event event = timeline.get(i); // gets the event/process
                    int x = 30 * (i + 1);  // positions the event in the panel ,setting (x,y) coordinates
                    int y = 20;
                    
                    g.drawRect(x, y, 30, 30); // draws a 30x30 rectangle
                    g.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    g.drawString(event.getProcessName(), x + 10, y + 20); // adds the event/process
                    g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    g.drawString(Integer.toString(event.getStartTime()), x - 5, y + 45); // adds the timeline
                    
                    if (i == timeline.size() - 1)
                    {
                        g.drawString(Integer.toString(event.getFinishTime()), x + 27, y + 45);
                    }
                }
            }
        }
        // called whenever the timeline of the event changes, such as when a new process is added or when a process is completed
        public void setTimeline(List<Event> timeline) // List of Events : an updated timeline of the events
        {
            this.timeline = timeline; // updates the timeline
            // app-triggered painting : allow program to asynchronously request a paint operation
            repaint(); 
        }
    }
}
