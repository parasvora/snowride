package cz.hudecekpetr.snowride.errors;

import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import cz.hudecekpetr.snowride.ui.Images;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.controlsfx.validation.Severity;


public class ErrorsTab {
    private final TableView<SnowrideError> tableErrors;

    public ErrorsTab(MainForm mainForm) {
        tableErrors = new ErrorsTablesView();
        tableErrors.getSelectionModel().setCellSelectionEnabled(false);
        tableErrors.setPlaceholder(new Label("Snowride detects no parse errors in this project."));
        TableColumn<SnowrideError, HighElement> locationColumn = new TableColumn<>("Location");
        locationColumn.setCellValueFactory(param -> param.getValue().where);
        locationColumn.setPrefWidth(150);

        locationColumn.setCellFactory(new Callback<TableColumn<SnowrideError, HighElement>, TableCell<SnowrideError, HighElement>>() {
            @Override
            public TableCell<SnowrideError, HighElement> call(TableColumn<SnowrideError, HighElement> param) {
                return new TableCell<SnowrideError, HighElement>() {
                    @Override
                    protected void updateItem(HighElement item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item.getShortName());
                            setGraphic(null);
                        }
                    }
                };
            }
        });
        tableErrors.getColumns().add(locationColumn);
        TableColumn<SnowrideError, Severity> severityColumn = new TableColumn<>("Severity");
        severityColumn.setCellValueFactory(param -> param.getValue().severity);
        severityColumn.setCellFactory(new Callback<TableColumn<SnowrideError, Severity>, TableCell<SnowrideError, Severity>>() {
            @Override
            public TableCell<SnowrideError, Severity> call(TableColumn<SnowrideError, Severity> param) {
                return new TableCell<SnowrideError, Severity>() {
                    @Override
                    protected void updateItem(Severity item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(null);
                            if (item == Severity.ERROR) {
                                setGraphic(new ImageView(Images.error));
                                setText("Error");
                            } else {
                                setGraphic(new ImageView(Images.warning));
                                setText("Warning");
                            }
                        }
                    }
                };
            }
        });
        severityColumn.setPrefWidth(100);
        tableErrors.getColumns().add(severityColumn);
        TableColumn<SnowrideError, ErrorKind> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(param -> param.getValue().type);
        typeColumn.setPrefWidth(100);
        tableErrors.getColumns().add(typeColumn);
        TableColumn<SnowrideError, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(param -> param.getValue().description);
        descriptionColumn.setPrefWidth(500);
        tableErrors.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() >= 2) {
                    SnowrideError selectedError = tableErrors.getFocusModel().getFocusedItem();
                    if (selectedError != null) {
                        mainForm.selectProgrammaticallyAndRememberInHistory(selectedError.where.getValue());
                    }
                }
            }
        });
        tableErrors.getColumns().add(descriptionColumn);
        HBox hErrors = new HBox(5, new Label("Double-click an error to switch to that file. Do 'Find usages' on any keyword to force-refresh import errors."));
        VBox vbErrors = new VBox(5, hErrors, tableErrors);
        VBox.setVgrow(tableErrors, Priority.ALWAYS);
        tab = new Tab("Errors", vbErrors);
        tab.setClosable(false);
        TreeItem<HighElement> root = mainForm.getProjectTree().getRoot();
        if (root != null) {
            tableErrors.setItems(root.getValue().allErrorsRecursive);
        }
        mainForm.getProjectTree().rootProperty().addListener(new ChangeListener<TreeItem<HighElement>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<HighElement>> observable, TreeItem<HighElement> oldValue, TreeItem<HighElement> newValue) {
                tableErrors.setItems(newValue.getValue().allErrorsRecursive);
            }
        });
    }
    public Tab tab;
}