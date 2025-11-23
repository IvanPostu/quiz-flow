import { FC, useState } from "react";
import { Modal } from "../Modal/Modal";
import * as styles from "./styles.module.scss";

const DEFAULT_RANDOM_NUMBER = 10;

interface ChoseQuestionsModalPropsType {
  modalIsActive: boolean;
  closeModal: () => void;
  selectQuestion: (id: string) => void;
  selectRandomQuestions: (count: number) => void;
  questionIds: string[];
  selectedQuestionIds: Set<string>;
}

export const ChoseQuestionsModal: FC<ChoseQuestionsModalPropsType> = (
  props
) => {
  const {
    closeModal,
    modalIsActive,
    questionIds,
    selectQuestion,
    selectedQuestionIds,
    selectRandomQuestions,
  } = props;
  const [state, setState] = useState<{
    randomNumber: number;
  }>({
    randomNumber: DEFAULT_RANDOM_NUMBER,
  });

  return (
    <Modal
      isOpen={modalIsActive}
      closeModal={closeModal}
      dismissOnClickOutside={false}
    >
      <div className={styles.rootContainer}>
        <h2 className={styles.title}>Choose questions:</h2>
        <div>
          <span>
            <b>Chosen: </b> {selectedQuestionIds.size}
          </span>
        </div>

        <div className={styles.checkboxesContainer}>
          <div className={styles.selectRandomlySection}>
            <button
              onClick={() => selectRandomQuestions(state.randomNumber)}
              disabled={
                state.randomNumber > questionIds.length ||
                state.randomNumber <= 0
              }
              className={styles.btn}
            >
              Choose randomly
            </button>
            <input
              type="number"
              value={state.randomNumber}
              onChange={(e) =>
                setState((prevState) => ({
                  ...prevState,
                  randomNumber: +e.target.value,
                }))
              }
            />
          </div>
          {questionIds.map((value, index) => (
            <label key={value}>
              <input
                type="checkbox"
                checked={selectedQuestionIds.has(value)}
                onChange={() => selectQuestion(value)}
              />
              {index}
            </label>
          ))}
        </div>
      </div>
    </Modal>
  );
};
