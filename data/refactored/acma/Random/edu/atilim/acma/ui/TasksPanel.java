package edu.atilim.acma.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.Timer;

import edu.atilim.acma.Core;
import edu.atilim.acma.TaskQueue;
import edu.atilim.acma.concurrent.ConcurrentTask;
import edu.atilim.acma.concurrent.SwitchMetricModeTask;
import edu.atilim.acma.ui.design.TasksPanelBase;

public class TasksPanel extends TasksPanelBase implements ActionListener {
    private long serialVersionUID = 1L;
    boolean paretoActive = Core.paretoMode;
    public TasksPanel() {
        Timer timer = new Timer(500, this);
        timer.setActionCommand("update");
        deleteButton.addActionListener(this);
        deleteAllButton.addActionListener(this);
        btnPareto.addActionListener(this);

        refresh();

        timer.start();
    }

    private void refresh() {
        Object selected = taskList.getSelectedValue();

        DefaultListModel model = new DefaultListModel();
        List<ConcurrentTask> tasks = TaskQueue.asList();
        for (ConcurrentTask task: tasks) {
            model.addElement(task);
        }
        taskList.setModel(model);

        if (selected != null)
            taskList.setSelectedValue(selected, true);
    }

    @Override
     public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("update")) {
            refresh();
        } else if (e.getActionCommand().equals("delete")) {
            Object selected = taskList.getSelectedValue();
            if (selected != null && selected instanceof ConcurrentTask)
                TaskQueue.remove((ConcurrentTask)selected);
            refresh();
        } else if (e.getActionCommand().equals("deleteall")) {
            while (TaskQueue.size() != 0)
                TaskQueue.pop();
            refresh();
        } else if (e.getActionCommand().equals("PARETO")) {
            paretoActive = !paretoActive;
            TaskQueue.push(new SwitchMetricModeTask(paretoActive));
        }
    }
}
