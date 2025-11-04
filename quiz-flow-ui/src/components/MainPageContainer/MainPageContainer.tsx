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
        <CardContainer />
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
      <Modal isOpen={state.takeQuizModalIsActive} closeModal={closeModal} />
    </Fragment>
  );
};

const cards = [
  {
    id: 1,
    icon: <IoCaretForwardCircleOutline className={styles.icon} />,
    title: "Take a Quiz",
    description: "Test your knowledge with a quick quiz!",
  },
  {
    id: 2,
    icon: <IoDocumentOutline className={styles.icon} />,
    title: "Manage Question Sets",
    description: "Create and manage question sets for your quizzes.",
  },
  {
    id: 3,
    icon: <IoBarChartOutline className={styles.icon} />,
    title: "Monitor Your Progress",
    description: "Track your scores and see how you're improving.",
  },
];

const CardContainer = () => {
  return (
    <Fragment>
      {cards.map((card) => {
        return (
          <div key={card.id} className={styles.card}>
            {card.icon}
            <h3 className={styles.title}>{card.title}</h3>
            <p className={styles.description}>{card.description}</p>
          </div>
        );
      })}
    </Fragment>
  );
};
