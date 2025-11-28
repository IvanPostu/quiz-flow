import { FC, useState } from "react";
import { Modal } from "../Modal/Modal";
import * as styles from "./styles.module.scss";
import { KeyValueContainer } from "../KeyValueContainer/KeyValueContainer";

const DEFAULT_RANDOM_NUMBER = 10;

interface ChoseQuestionsModalPropsType {
  modalIsActive: boolean;
  closeModal: () => void;
  selectQuestion: (id: string) => void;
  selectRandomQuestions: (count: number) => void;
  selectByOffsetAndLimit: (offset: number, limit: number) => void;
  questionIds: string[];
  selectedQuestionIds: Set<string>;
}

interface ChoseQuestionsModalStateType {
  randomNumber: number;
  offset: number;
  limit: number;
}

const DEFAULT_STATE: ChoseQuestionsModalStateType = {
  limit: 10,
  offset: 0,
  randomNumber: 10,
};

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
    selectByOffsetAndLimit,
  } = props;
  const [state, setState] =
    useState<ChoseQuestionsModalStateType>(DEFAULT_STATE);

  return (
    <Modal
      isOpen={modalIsActive}
      closeModal={closeModal}
      dismissOnClickOutside={false}
    >
      <div className={styles.rootContainer}>
        <h2 className={styles.title}>Choose questions:</h2>
        <KeyValueContainer
          data={{
            "Total:": "" + questionIds.length,
            "Chosen:": "" + selectedQuestionIds.size,
          }}
        />

        <div className={styles.checkboxesContainer}>
          <div className={styles.selectSection}>
            <button
              onClick={() => selectRandomQuestions(state.randomNumber)}
              disabled={
                state.randomNumber > questionIds.length ||
                state.randomNumber <= 0
              }
              className={styles.btn}
            >
              Choose
            </button>
            <span>Randomly</span>
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
          <div className={styles.selectSection}>
            <button
              onClick={() => selectByOffsetAndLimit(state.offset, state.limit)}
              disabled={
                state.offset + state.limit > questionIds.length ||
                state.offset < 0 ||
                state.limit <= 0
              }
              className={styles.btn}
            >
              Choose
            </button>
            <span>Offset</span>
            <input
              type="number"
              value={state.offset}
              onChange={(e) =>
                setState((prevState) => ({
                  ...prevState,
                  offset: +e.target.value,
                }))
              }
            />
            <span>Limit</span>
            <input
              type="number"
              value={state.limit}
              onChange={(e) =>
                setState((prevState) => ({
                  ...prevState,
                  limit: +e.target.value,
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
