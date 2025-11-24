import { useLocation, useNavigate } from "react-router-dom";
import { Container } from "../Container/Container";
import * as styles from "./styles.module.scss";
import { CardContainer } from "../CardContainer/CardContainer";
import { FC, Fragment, useEffect, useState } from "react";
import * as questionsets from "src/model/questionsets/questionsets";
import * as questions from "src/model/questions/questions";
import * as quizzes from "src/model/quizzes/quizzes";
import { QuestionSet } from "src/model/questionsets/QuestionSet";
import { useAppSelector } from "src/redux";
import { selectAccessToken } from "src/redux/authentication/authenticationSlice";
import { useIsMounted } from "src/hooks/useIsMounted";
import { LoaderSpinner } from "../LoaderSpinner/LoaderSpinner";
import {
  Question,
  QuestionSetVersion,
} from "src/model/questions/QuestionSetVersion";
import { ListOfItems } from "../ListOfItems/ListOfItems";
import { ChoseQuestionsModal } from "../ChoseQuestionsModal/ChoseQuestionsModal";
import { randomArrayElements } from "src/model/utils/randomArrayElements";
import { BlurOverlay } from "../BlurOverlay/BlurOverlay";
import { Quiz } from "src/model/quizzes/Quiz";
import {
  calculateData,
  KeyValueContainer,
} from "../KeyValueContainer/KeyValueContainer";

interface QuizSetupContainerStateType {
  questionSet: QuestionSet | null;
  questionSetVersion: QuestionSetVersion | null;
  choseQuestionsModalIsActive: boolean;
  selectedQuestionIds: Set<string>;
  isQuizCreateRequestOngoing: boolean;
}

export const QuizSetupContainer = () => {
  const [state, setState] = useState<QuizSetupContainerStateType>({
    questionSet: null,
    questionSetVersion: null,
    choseQuestionsModalIsActive: false,
    selectedQuestionIds: new Set(),
    isQuizCreateRequestOngoing: false,
  });
  const navigate = useNavigate();
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const accessToken = useAppSelector(selectAccessToken) || "";
  const isMounted = useIsMounted();

  const questionSetId = searchParams.get("questionSetId") || "";
  useEffect(() => {
    fetchQuestionSet(accessToken, questionSetId, (questionSetResult) => {
      if (!isMounted()) {
        return;
      }
      if (questionSetResult === "error") {
        return;
      }

      setState((prevState) => ({
        ...prevState,
        questionSet: questionSetResult,
      }));
      fetchQuestionSetVersion(
        accessToken,
        questionSetId,
        questionSetResult.latestVersion,
        (questionSetVersionResult) => {
          if (!isMounted()) {
            return;
          }
          if (questionSetVersionResult === "error") {
            return;
          }
          setState((prevState) => ({
            ...prevState,
            questionSetVersion: questionSetVersionResult,
          }));
        }
      );
    });
  }, []);

  return (
    <Container>
      {state.isQuizCreateRequestOngoing && <OverlayLoader />}
      <CardContainer className={styles.rootContainer}>
        <div className={styles.titleContainer}>
          <h2>Quiz Setup</h2>
        </div>
        {state.questionSet === null ? (
          <LoaderSpinner />
        ) : (
          <KeyValueContainer data={calculateData(state.questionSet)} />
        )}
        {state.questionSetVersion === null ? null : (
          <Fragment>
            <div className={styles.setupSection}>
              <button
                onClick={() => {
                  setState((prevState) => ({
                    ...prevState,
                    choseQuestionsModalIsActive: true,
                  }));
                }}
                className={styles.btn}
              >
                Chose questions
              </button>
              <div>
                <b>Chosen:</b>
                <span>{state.selectedQuestionIds.size}</span>
              </div>
              <div style={{ flexBasis: "100%" }}>
                <button
                  onClick={() => {
                    if (
                      !state.questionSet ||
                      state.selectedQuestionIds.size === 0
                    ) {
                      return;
                    }
                    setState((prevState) => ({
                      ...prevState,
                      isQuizCreateRequestOngoing: true,
                    }));
                    createQuiz(
                      accessToken,
                      state.questionSet.id,
                      state.questionSet.latestVersion,
                      Array.from(state.selectedQuestionIds),
                      (quizResult) => {
                        if (!isMounted()) {
                          return;
                        }
                        setState((prevState) => ({
                          ...prevState,
                          isQuizCreateRequestOngoing: false,
                        }));
                        if (quizResult === "error") {
                          return;
                        }
                        navigate(`/quiz/${quizResult.id}`);
                      }
                    );
                  }}
                  disabled={
                    state.selectedQuestionIds.size === 0 ||
                    state.isQuizCreateRequestOngoing
                  }
                  className={styles.btn}
                >
                  Begin quiz
                </button>
              </div>
            </div>
            <ListOfQuestions
              questions={state.questionSetVersion.questions}
              onItemClick={() => {}}
            />
          </Fragment>
        )}
      </CardContainer>
      <ChoseQuestionsModal
        selectRandomQuestions={(count: number) => {
          const questionIds = randomArrayElements(
            state.questionSetVersion?.questions || [],
            count
          ).map((value) => value.id);

          setState((prevState) => {
            const newSet = new Set<string>([...questionIds]);
            return {
              ...prevState,
              selectedQuestionIds: newSet,
            };
          });
        }}
        modalIsActive={state.choseQuestionsModalIsActive}
        closeModal={() =>
          setState((prevState) => ({
            ...prevState,
            choseQuestionsModalIsActive: false,
          }))
        }
        selectQuestion={(id) => {
          setState((prevState) => {
            const newSet = new Set(prevState.selectedQuestionIds);
            if (newSet.has(id)) {
              newSet.delete(id);
            } else {
              newSet.add(id);
            }
            return {
              ...prevState,
              selectedQuestionIds: newSet,
            };
          });
        }}
        questionIds={(state.questionSetVersion?.questions || []).map(
          (value) => value.id
        )}
        selectedQuestionIds={state.selectedQuestionIds}
      />
    </Container>
  );
};

function OverlayLoader() {
  return (
    <BlurOverlay>
      <LoaderSpinner />
    </BlurOverlay>
  );
}

const ListOfQuestions: FC<{
  questions: Question[];
  onItemClick: (id: string) => void;
}> = (props) => {
  return (
    <div>
      <ListOfItems
        items={props.questions.map((value, index) => ({
          id: value.id,
          title: value.question,
          descriptionItems: [],
          left: <span>{index}) </span>,
        }))}
        onItemClick={props.onItemClick}
      />
    </div>
  );
};

async function fetchQuestionSet(
  accessToken: string,
  questionSetId: string,
  onComplete: (questionSets: QuestionSet | "error") => void
) {
  try {
    const result = await questionsets.getQuestionSet(
      accessToken,
      questionSetId
    );
    onComplete(result);
  } catch (e) {
    console.error(e);
    onComplete("error");
  }
}

async function fetchQuestionSetVersion(
  accessToken: string,
  questionSetId: string,
  version: number,
  onComplete: (questionSetVersion: QuestionSetVersion | "error") => void
) {
  try {
    const result = await questions.getQuestionSetVersion(
      accessToken,
      questionSetId,
      version
    );
    onComplete(result);
  } catch (e) {
    console.error(e);
    onComplete("error");
  }
}

async function createQuiz(
  accessToken: string,
  questionSetId: string,
  questionSetVersion: number,
  questionIds: string[],
  onComplete: (quiz: Quiz | "error") => void
) {
  try {
    const result = await quizzes.createQuiz(accessToken, {
      question_ids: questionIds,
      question_set_id: questionSetId,
      question_set_version: questionSetVersion,
    });
    onComplete(result);
  } catch (e) {
    console.error(e);
    onComplete("error");
  }
}
