import { MouseEventHandler, PropsWithChildren } from "react";
import * as styles from "./styles.module.scss";
import { IoCloseOutline } from "react-icons/io5";

interface PropsType {
  isOpen: boolean;
  closeModal: () => void;
}

const Modal: React.FC<PropsWithChildren<PropsType>> = (props) => {
  const { closeModal, isOpen, children } = props;
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
        {children}
      </div>
    </div>
  );
};

export { Modal };
