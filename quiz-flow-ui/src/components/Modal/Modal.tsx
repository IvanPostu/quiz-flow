import { MouseEventHandler } from "react";
import * as styles from "./styles.module.scss";
import { IoCloseOutline } from "react-icons/io5";

interface PropsType {
  isOpen: boolean;
  closeModal: () => void;
}

const Modal = (props: PropsType) => {
  const { closeModal, isOpen } = props;
  if (!isOpen) return null;

  const handleBackgroundClick: MouseEventHandler<HTMLDivElement> = (e) => {
    if (e.target === e.currentTarget) {
      closeModal();
    }
  };

  return (
    <div className={styles.modalBackdrop} onClick={handleBackgroundClick}>
      <div className={styles.modalContent}>
        <IoCloseOutline className={styles.closeButton} onClick={closeModal} />

        <div className={styles.modalBody}>
          <h2>Modal Content</h2>
          <p>This is the content inside the modal.</p>
        </div>
      </div>
    </div>
  );
};

export { Modal };
