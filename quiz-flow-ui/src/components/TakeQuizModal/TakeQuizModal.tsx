import { useIsMounted } from "src/hooks/useIsMounted";
import { LoaderSpinner } from "../LoaderSpinner/LoaderSpinner";
import { Modal } from "../Modal/Modal";
import * as questionsets from "src/model/questionsets/questionsets";
import { QuestionSet } from "src/model/questionsets/QuestionSet";
import { useEffect, useState } from "react";
import { useAppSelector } from "src/redux";
import { selectAccessToken } from "src/redux/authentication/authenticationSlice";
import * as styles from "./styles.module.scss";
import { ListOfItems } from "../ListOfItems/ListOfItems";
import { useNavigate } from "react-router-dom";

const DEFAULT_LIMIT_OF_QUESTION_SETS = 10;

interface TakeQuizModalPropsType {
  takeQuizModalIsActive: boolean;
  closeModal: () => void;
}

interface TakeQuizModalStateType {
  questionSets: QuestionSet[] | null;
  atLeastOneTimeWasOpened: boolean;
  offset: number;
}

export const TakeQuizModal = ({
  takeQuizModalIsActive,
  closeModal,
}: TakeQuizModalPropsType) => {
  const [state, setState] = useState<TakeQuizModalStateType>({
    questionSets: null,
    atLeastOneTimeWasOpened: takeQuizModalIsActive,
    offset: 0,
  });
  const accessToken = useAppSelector(selectAccessToken) || "";
  const isMounted = useIsMounted();
  const navigate = useNavigate();
  const internalFetchQuestionSets = (offset: number) => {
    fetchQuestionSets(
      accessToken,
      DEFAULT_LIMIT_OF_QUESTION_SETS,
      offset,
      (result) => {
        if (!isMounted()) {
          return;
        }
        if (result === "error") {
          alert("Something went wrong while retrieving the question sets list");
        } else {
          if (result.length === 0) {
            return;
          }
          setState((prevState) => ({
            ...prevState,
            questionSets: result,
            offset: offset,
          }));
        }
      }
    );
  };
  const fetchQuestionSetsList = (type: "next" | "prev") => {
    let newOffset =
      state.offset +
      (type === "next"
        ? DEFAULT_LIMIT_OF_QUESTION_SETS
        : -DEFAULT_LIMIT_OF_QUESTION_SETS);
    if (newOffset < 0) {
      newOffset = 0;
    }
    internalFetchQuestionSets(newOffset);
  };

  useEffect(() => {
    if (takeQuizModalIsActive && !state.atLeastOneTimeWasOpened) {
      setState((prevState) => ({
        ...prevState,
        atLeastOneTimeWasOpened: true,
      }));
      internalFetchQuestionSets(state.offset);
    }
  }, [takeQuizModalIsActive]);

  return (
    <Modal
      isOpen={takeQuizModalIsActive}
      closeModal={closeModal}
      dismissOnClickOutside={false}
    >
      <div className={styles.rootContainer}>
        <h2 className={styles.title}>Take a Quiz</h2>
        <p>
          <b>Please choose a question set for the quiz:</b>
        </p>
        {state.questionSets === null ? (
          <LoaderSpinner />
        ) : (
          <QuestionSetsList
            questionSets={state.questionSets}
            onItemClick={(id) => {
              navigate(`/setup-quiz?questionSetId=${id}`);
            }}
            onNextClick={() => fetchQuestionSetsList("next")}
            onPrevClick={() => fetchQuestionSetsList("prev")}
          />
        )}
      </div>
    </Modal>
  );
};

const QuestionSetsList: React.FC<{
  questionSets: QuestionSet[];
  onItemClick: (id: string) => void;
  onNextClick: () => void;
  onPrevClick: () => void;
}> = (props) => {
  return (
    <div>
      <ListOfItems
        listContainerStyle={{ maxHeight: 300 }}
        items={props.questionSets.map((value) => ({
          id: value.id,
          title: value.name,
          descriptionItems: [],
        }))}
        onItemClick={props.onItemClick}
      />
      <div className={styles.paginationSection}>
        <button
          onClick={props.onPrevClick}
          className={styles.simplePaginationButton}
        >
          Prev. 10
        </button>
        <button
          onClick={props.onNextClick}
          className={styles.simplePaginationButton}
        >
          Next 10
        </button>
      </div>
    </div>
  );
};

async function fetchQuestionSets(
  accessToken: string,
  limit: number,
  offset: number,
  onComplete: (questionSets: QuestionSet[] | "error") => void
) {
  try {
    const result = await questionsets.list(accessToken, limit, offset, "DESC");
    onComplete(result);
  } catch (e) {
    console.error(e);
    onComplete("error");
  }
}
