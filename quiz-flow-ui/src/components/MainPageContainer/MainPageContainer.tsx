import {
  IoBarChartOutline,
  IoCaretForwardCircleOutline,
  IoDocumentOutline,
} from "react-icons/io5";
import * as styles from "./styles.module.scss";
import { Fragment } from "react/jsx-runtime";
import { useState } from "react";
import { Modal } from "../Modal/Modal";

export const MainPageContainer = () => {
  const [state, setState] = useState<{ takeQuizModalIsActive: boolean }>({
    takeQuizModalIsActive: false,
  });
  const closeModal = () =>
    setState((prevState) => ({ ...prevState, takeQuizModalIsActive: false }));

  return (
    <Fragment>
      <div className={styles.rootContainer}>
        <CardContainer
          onTakeQuizCardClick={() =>
            setState((prevState) => ({
              ...prevState,
              takeQuizModalIsActive: true,
            }))
          }
        />
      </div>
      <button
        onClick={() => {
          setState((prevState) => ({
            ...prevState,
            takeQuizModalIsActive: true,
          }));
        }}
      >
        Open Modal
      </button>
      <Modal isOpen={state.takeQuizModalIsActive} closeModal={closeModal}>
        <div>
          <h2>Take a Quiz</h2>
          <p>This is the content inside the modal.</p>
        </div>
      </Modal>
    </Fragment>
  );
};

const CardContainer: React.FC<{
  onTakeQuizCardClick: () => void;
}> = ({ onTakeQuizCardClick }) => {
  const cards = [
    {
      id: 1,
      onClick: onTakeQuizCardClick,
      icon: <IoCaretForwardCircleOutline className={styles.icon} />,
      title: "Take a Quiz",
      description: "Test your knowledge with a quick quiz!",
    },
    {
      id: 2,
      onClick: () => {},
      icon: <IoDocumentOutline className={styles.icon} />,
      title: "Manage Question Sets",
      description: "Create and manage question sets for your quizzes.",
    },
    {
      id: 3,
      onClick: () => {},
      icon: <IoBarChartOutline className={styles.icon} />,
      title: "Monitor Your Progress",
      description: "Track your scores and see how you're improving.",
    },
  ];

  return (
    <Fragment>
      {cards.map((card) => {
        return (
          <div key={card.id} onClick={card.onClick} className={styles.card}>
            {card.icon}
            <h3 className={styles.title}>{card.title}</h3>
            <p className={styles.description}>{card.description}</p>
          </div>
        );
      })}
    </Fragment>
  );
};
