import React, { useState, useEffect } from "react";
import * as styles from "./styles.module.scss";
import { Toast, ToastType } from "./ToastContext";

interface ToastNotificationProps {
  notifications: Toast[];
  removeNotification: (id: number) => void;
}

const TYPE_NAMES_BY_TYPE: Record<ToastType, string> = {
  error: "Error",
  success: "Success",
  warning: "Warning",
  info: "Info",
};

const ToastNotification: React.FC<ToastNotificationProps> = ({
  notifications,
  removeNotification,
}) => {
  const [exiting, setExiting] = useState<number[]>([]);

  useEffect(() => {
    notifications.forEach((notification) => {
      const timer = setTimeout(() => handleExit(notification.id), 3000);
      return () => clearTimeout(timer);
    });
  }, [notifications]);

  const handleExit = (id: number) => {
    setExiting((prev) => [...prev, id]);
    setTimeout(() => removeNotification(id), 300); // wait for exit animation
  };

  return (
    <div className={styles.toastContainer}>
      {notifications.map((notification) => {
        const classNameValue = `${
          exiting.includes(notification.id) ? styles.exit : ""
        } ${styles.toast}`;

        return (
          <div
            key={notification.id}
            className={classNameValue}
            data-status={notification.type}
          >
            <div className={styles.status}>
              {TYPE_NAMES_BY_TYPE[notification.type]}
            </div>
            <div className={styles.message}>{notification.message}</div>
          </div>
        );
      })}
    </div>
  );
};

export default ToastNotification;
