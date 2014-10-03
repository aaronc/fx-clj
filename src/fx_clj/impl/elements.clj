(ns ^:no-doc fx-clj.impl.elements
  (:require
    [fx-clj.impl.bootstrap])
  (:import (javafx.scene.control.cell CheckBoxTreeCell CheckBoxTreeTableCell
                                      CheckBoxTableCell CheckBoxListCell
                                      ChoiceBoxListCell ChoiceBoxTableCell
                                      ChoiceBoxTreeCell ChoiceBoxTreeTableCell
                                      TextFieldTreeTableCell TextFieldTreeCell
                                      ProgressBarTableCell
                                      ProgressBarTreeTableCell
                                      ComboBoxTreeTableCell ComboBoxTreeCell
                                      ComboBoxTableCell ComboBoxListCell
                                      TextFieldListCell TextFieldTableCell)
           (javafx.scene.control CheckMenuItem ButtonBase Button Accordion Cell
                                 CheckBox ChoiceBox ColorPicker ComboBox
                                 ComboBoxBase TextField TreeView TreeTableView
                                 TreeTableRow TreeTableColumn TreeTableCell
                                 TreeCell Tooltip ToolBar ToggleButton
                                 TitledPane TextInputControl ProgressBar
                                 ProgressIndicator PopupControl PasswordField
                                 Pagination MenuItem MenuButton MenuBar Menu
                                 ListView ListCell Labeled Label IndexedCell
                                 Hyperlink DatePicker DateCell CustomMenuItem
                                 Control ContextMenu RadioButton RadioMenuItem
                                 ScrollBar ScrollPane Separator
                                 SeparatorMenuItem Slider SplitMenuButton
                                 SplitPane Tab TableCell TableColumn
                                 TableColumnBase TableRow TableView TabPane
                                 TextArea TreeItem ToggleGroup)
           (javafx.scene.chart BarChart Axis AreaChart BubbleChart CategoryAxis
                               Chart XYChart ValueAxis PieChart NumberAxis
                               LineChart ScatterChart StackedAreaChart
                               StackedBarChart)
           (javafx.scene.layout BorderPane AnchorPane VBox TilePane Pane HBox
                                GridPane FlowPane Region StackPane)
           (javafx.scene.shape Arc Box Circle Polyline Polygon Path MeshView
                               Line Ellipse Cylinder CubicCurve QuadCurve
                               Rectangle Shape Shape3D Sphere SVGPath)
           (javafx.scene AmbientLight Camera PointLight PerspectiveCamera
                         Parent ParallelCamera Node LightBase Group SubScene)
           (javafx.scene.canvas Canvas)
           (javafx.scene.web WebView HTMLEditor)
           (javafx.scene.text TextFlow Text)
           (javafx.scene.media MediaView)
           (javafx.scene.image ImageView)
           (javafx.embed.swing SwingNode)
           (java.lang.reflect Modifier Constructor)
           (javafx.stage PopupWindow Stage Popup DirectoryChooser FileChooser)))

(def element-factories (atom {}))

(defn get-default-ctr [cls]
  (try
    (.getConstructor cls nil)
    (catch Throwable t)))

(defn register-classes [class-list]
  (doseq [cls class-list]
    (let [^Constructor ctr (get-default-ctr cls)]
      (when (and ctr (not (Modifier/isAbstract (.getModifiers cls))))
        (swap! element-factories
               assoc
               (.getSimpleName cls)
               (with-meta
                 (fn []
                   (.newInstance ctr nil))
                 {::constructor ctr ::class cls}))))))

(def styleable-classes
  [
    Accordion, AmbientLight, AnchorPane, Arc, AreaChart, Axis,
    BarChart, BorderPane, Box, BubbleChart, Button, ButtonBase,
    Camera, Canvas, CategoryAxis, Cell, Chart, CheckBox,
    CheckBoxListCell, CheckBoxTableCell, CheckBoxTreeCell,
    CheckBoxTreeTableCell, CheckMenuItem, ChoiceBox,
    ChoiceBoxListCell, ChoiceBoxTableCell, ChoiceBoxTreeCell,
    ChoiceBoxTreeTableCell, Circle, ColorPicker, ComboBox,
    ComboBoxBase, ComboBoxListCell, ComboBoxTableCell,
    ComboBoxTreeCell, ComboBoxTreeTableCell, ContextMenu, Control,
    CubicCurve, CustomMenuItem, Cylinder, DateCell, DatePicker,
    Ellipse, FlowPane, GridPane, Group, HBox, HTMLEditor, Hyperlink,
    ImageView, IndexedCell, Label, Labeled, LightBase, Line,
    LineChart, ListCell, ListView, MediaView, Menu, MenuBar,
    MenuButton, MenuItem, MeshView, Node, NumberAxis,
    ;; Pagination,
    Pane, ParallelCamera, Parent, PasswordField, Path,
    PerspectiveCamera, PieChart, PointLight, Polygon, Polyline,
    PopupControl,
    ;; PopupControl.CSSBridge,
    ProgressBar,
    ProgressBarTableCell, ProgressBarTreeTableCell, ProgressIndicator,
    QuadCurve, RadioButton, RadioMenuItem, Rectangle, Region,
    ScatterChart, ScrollBar, ScrollPane, Separator, SeparatorMenuItem,
    Shape, Shape3D, Slider, Sphere, SplitMenuButton, SplitPane,
    StackedAreaChart, StackedBarChart, StackPane, SubScene, SVGPath,
    SwingNode, Tab, TableCell, TableColumn, TableColumnBase, TableRow,
    TableView, TabPane, Text, TextArea, TextField, TextFieldListCell,
    TextFieldTableCell, TextFieldTreeCell, TextFieldTreeTableCell,
    TextFlow, TextInputControl, TilePane, TitledPane, ToggleButton,
    ToolBar, Tooltip, TreeCell, TreeTableCell, TreeTableColumn,
    TreeTableRow, TreeTableView, TreeView, ValueAxis, VBox, WebView,
    XYChart
    ])

(register-classes styleable-classes)

(def window-classes
  [PopupWindow Stage Popup])

(register-classes window-classes)

(def other-classes
  [TreeItem FileChooser DirectoryChooser ToggleGroup])

(register-classes other-classes)
