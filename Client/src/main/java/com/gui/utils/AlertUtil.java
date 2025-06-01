package com.gui.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class AlertUtil {

    // Приватный конструктор, чтобы предотвратить создание экземпляра класса
    private AlertUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Показывает диалоговое окно с сообщением об ошибке.
     *
     * @param header  Заголовок сообщения
     * @param content Основное содержимое сообщения
     */
    public static void error(String header, String content) {
        showAlert(Alert.AlertType.ERROR, header, content);
    }

    /**
     * Показывает диалоговое окно с предупреждением.
     *
     * @param header  Заголовок сообщения
     * @param content Основное содержимое сообщения
     */
    public static void warning(String header, String content) {
        showAlert(Alert.AlertType.WARNING, header, content);
    }

    /**
     * Показывает информационное диалоговое окно.
     *
     * @param header  Заголовок сообщения
     * @param content Основное содержимое сообщения
     */
    public static void info(String header, String content) {
        showAlert(Alert.AlertType.INFORMATION, header, content);
    }

    /**
     * Показывает диалог подтверждения и возвращает результат.
     *
     * @param header  Заголовок сообщения
     * @param content Основное содержимое сообщения
     * @return ButtonType.OK, если пользователь подтвердил, или ButtonType.CANCEL, если отменил
     */
    public static ButtonType confirmation(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText(header);
        alert.setContentText(content);

        return alert.showAndWait().orElse(ButtonType.CANCEL);
    }

    /**
     * Вспомогательный метод для показа диалогового окна.
     *
     * @param alertType Тип диалогового окна (ERROR, WARNING, INFORMATION)
     * @param header    Заголовок сообщения
     * @param content   Основное содержимое сообщения
     */
    private static void showAlert(Alert.AlertType alertType, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Сообщение");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}