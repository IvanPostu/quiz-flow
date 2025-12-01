import { useIsMounted } from "src/hooks/useIsMounted";
import { LoaderSpinner } from "../LoaderSpinner/LoaderSpinner";
import { Modal } from "../Modal/Modal";
import * as questionsets from "src/model/questionsets/questionsets";
import { QuestionSet } from "src/model/questionsets/QuestionSet";
import { FC, Fragment, useEffect, useState } from "react";
import { useAppSelector } from "src/redux";
import { selectAccessToken } from "src/redux/authentication/authenticationSlice";
import * as styles from "./styles.module.scss";
import { ListOfItems } from "../ListOfItems/ListOfItems";
import { useNavigate } from "react-router-dom";
import { Tabs } from "../Tabs/Tabs";

const DEFAULT_LIMIT_OF_QUESTION_SETS = 10;

type QuestionSetType = "local" | "global";

interface TakeQuizModalPropsType {
  takeQuizModalIsActive: boolean;
  closeModal: () => void;
}

interface TakeQuizModalStateType {
  questionSets: QuestionSet[] | null;
  atLeastOneTimeWasOpened: boolean;
  offset: number;
  questionSetType: QuestionSetType;
}

export const TakeQuizModal = ({
  takeQuizModalIsActive,
  closeModal,
}: TakeQuizModalPropsType) => {
  const [state, setState] = useState<TakeQuizModalStateType>({
    questionSets: null,
    atLeastOneTimeWasOpened: takeQuizModalIsActive,
    offset: 0,
    questionSetType: "local",
  });
  const accessToken = useAppSelector(selectAccessToken) || "";
  const isMounted = useIsMounted();
  const navigate = useNavigate();
  const internalFetchQuestionSets = (
    offset: number,
    questionSetType: QuestionSetType
  ) => {
    fetchQuestionSets(
      accessToken,
      DEFAULT_LIMIT_OF_QUESTION_SETS,
      offset,
      questionSetType,
      (result) => {
        if (!isMounted()) {
          return;
        }
        if (result === "error") {
          return;
        }
        setState((prevState) => ({
          ...prevState,
          questionSets: result,
          offset: offset,
          questionSetType: questionSetType,
        }));
      }
    );
  };
  const fetchQuestionSetsList = (
    type: "next" | "prev",
    questionSetType: QuestionSetType
  ) => {
    let newOffset =
      state.offset +
      (type === "next"
        ? DEFAULT_LIMIT_OF_QUESTION_SETS
        : -DEFAULT_LIMIT_OF_QUESTION_SETS);
    if (newOffset < 0) {
      newOffset = 0;
    }
    internalFetchQuestionSets(newOffset, questionSetType);
  };

  useEffect(() => {
    if (takeQuizModalIsActive && !state.atLeastOneTimeWasOpened) {
      setState((prevState) => ({
        ...prevState,
        atLeastOneTimeWasOpened: true,
      }));
      internalFetchQuestionSets(state.offset, "local");
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

        <Tabs
          tabs={[
            {
              id: "local",
              label: "Local",
              content: (
                <TabContent
                  questionSets={state.questionSets}
                  onItemClick={(id) => {
                    navigate(`/setup-quiz?questionSetId=${id}`);
                  }}
                  onNextClick={() => fetchQuestionSetsList("next", "local")}
                  onPrevClick={() => fetchQuestionSetsList("prev", "local")}
                />
              ),
            },
            {
              id: "global",
              label: "Global",
              content: (
                <TabContent
                  questionSets={state.questionSets}
                  onItemClick={(id) => {
                    navigate(`/setup-quiz?questionSetId=${id}`);
                  }}
                  onNextClick={() => fetchQuestionSetsList("next", "global")}
                  onPrevClick={() => fetchQuestionSetsList("prev", "global")}
                />
              ),
            },
          ]}
          onTabChanged={(id) => {
            if (id === "local") {
              internalFetchQuestionSets(0, "local");
            } else if (id === "global") {
              internalFetchQuestionSets(0, "global");
            }
          }}
        />
      </div>
    </Modal>
  );
};

const TabContent: FC<{
  questionSets: QuestionSet[] | null;
  onItemClick: (id: string) => void;
  onNextClick: () => void;
  onPrevClick: () => void;
}> = (props) => {
  if (!props.questionSets) {
    return <LoaderSpinner />;
  }

  const { onItemClick, onNextClick, onPrevClick, questionSets } = props;

  return (
    <Fragment>
      <QuestionSetsList
        questionSets={questionSets}
        onItemClick={onItemClick}
        onNextClick={onNextClick}
        onPrevClick={onPrevClick}
      />
    </Fragment>
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
  questionSetType: QuestionSetType,
  onComplete: (questionSets: QuestionSet[] | "error") => void
) {
  try {
    let result: QuestionSet[];
    if (questionSetType === "local") {
      result = await questionsets.list(accessToken, limit, offset, "DESC");
    } else {
      result = await questionsets.listGlobal(
        accessToken,
        limit,
        offset,
        "DESC"
      );
    }
    onComplete(result);
  } catch (e) {
    console.error(e);
    onComplete("error");
  }
}
