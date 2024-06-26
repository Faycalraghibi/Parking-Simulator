package com.example.parkingsimulator;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class CustomRectangle extends StackPane {
    private Rectangle rectangle;
    private Text label;

    public CustomRectangle() {
        rectangle = new Rectangle(50, 50, Color.LIGHTGRAY);
        rectangle.setStroke(Color.BLACK);
        label = new Text();
        label.setFont(new Font("Arial", 12));
        this.getChildren().addAll(rectangle, label);
        this.setStyle("-fx-padding: 5;");
    }

    public void setColor(Color color) {
        rectangle.setFill(color);
    }

    public void setText(String text) {
        label.setText(text);
    }
}
