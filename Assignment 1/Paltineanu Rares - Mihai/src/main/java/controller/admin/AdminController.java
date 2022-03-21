package controller.admin;

import model.User;
import model.validator.UserValidator;
import repository.security.RightsRolesRepository;
import service.action.ActionService;
import service.user.UserService;
import view.admin.ActionEmployeeView;
import view.admin.AdminView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class AdminController {
    private static final String[] tableHeader = new String[]{"Id", "Username", "Password"};
    private final AdminView adminView;
    private final UserValidator userValidator;
    private final UserService userService;
    private final ActionService actionService;
    private final RightsRolesRepository rightsRolesRepository;

    public AdminController(AdminView adminView, UserValidator userValidator, UserService userService, ActionService actionService, RightsRolesRepository rightsRolesRepository) {
        this.adminView = adminView;
        this.userValidator = userValidator;
        this.userService = userService;
        this.actionService = actionService;
        this.rightsRolesRepository = rightsRolesRepository;

        new GenerateReportController(adminView, actionService);

        setTableHeader();
        initializeButtonsListener();
    }

    private void initializeButtonsListener() {
        this.adminView.setViewEmployeesButtonListener(new ViewUsersButtonListener());
        this.adminView.setAddEmployeeButtonListener(new AddUserButtonListener());
        this.adminView.setDeleteEmployeeButtonListener(new DeleteUserButtonListener());
        this.adminView.setUpdateEmployeeButtonListener(new UpdateUserButtonListener());
        this.adminView.getViewEmployeesButton().doClick();
    }

    private void setTableHeader() {
        DefaultTableModel defaultTableModel = (DefaultTableModel) adminView.getEmployeesTable().getModel();
        defaultTableModel.setColumnIdentifiers(tableHeader);
    }

    private class AddUserButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            new AddEmployeeController(adminView, new ActionEmployeeView(), userService, userValidator, rightsRolesRepository);
        }
    }

    private class UpdateUserButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(!isOneUserSelected()) {
                JOptionPane.showMessageDialog(adminView.getContentPane(),
                        "Please select one user");
                return;
            }
            int selectedRow = adminView.getEmployeesTable().getSelectedRow();
            Long id = Long.parseLong((String) adminView.getEmployeesTable().getValueAt(selectedRow, 0));
            String username = (String) adminView.getEmployeesTable().getValueAt(selectedRow, 1);

            ActionEmployeeView updateEmployeeView = new ActionEmployeeView();
            updateEmployeeView.setTitle("Update employee");

            if(username.equals("admin")) {
                updateEmployeeView.getUsernameField().setEditable(false);
            }
            new UpdateEmployeeController(updateEmployeeView, adminView, userValidator, userService, id);
        }
    }

    private class DeleteUserButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(!isOneUserSelected()) {
                JOptionPane.showMessageDialog(adminView.getContentPane(),
                        "Please select one user");
                return;
            }

            int selectedRow = adminView.getEmployeesTable().getSelectedRow();
            Long id = Long.parseLong((String) adminView.getEmployeesTable().getValueAt(selectedRow, 0));
            String username = (String) adminView.getEmployeesTable().getValueAt(selectedRow, 1);

            if(username.equals("admin")) {
                JOptionPane.showMessageDialog(adminView.getContentPane(), "You can not delete your own user");
                return;
            }

            boolean flag = userService.delete(id);

            if(flag) {
                JOptionPane.showMessageDialog(adminView.getContentPane(), "Delete successful");
                adminView.getViewEmployeesButton().doClick();
            } else {
                JOptionPane.showMessageDialog(adminView.getContentPane(), "An error occurred while trying to delete user with " +
                        "id " + id);
            }
        }
    }

    private class ViewUsersButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            List<User> users = userService.findAll();
            DefaultTableModel defaultTableModel = (DefaultTableModel) adminView.getEmployeesTable().getModel();
            defaultTableModel.setRowCount(0);
            for(User user: users) {
                final String[] row = new String[]{String.valueOf(user.getId()),
                        user.getUsername(),
                        "********"};
                defaultTableModel.addRow(row);
            }
        }
    }

    private boolean isOneUserSelected() {
        return adminView.getEmployeesTable().getSelectedRows().length == 1;
    }

    public void setViewVisible() {
        adminView.setVisible(true);
    }
}
