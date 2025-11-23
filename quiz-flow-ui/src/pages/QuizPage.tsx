import { useParams } from "react-router-dom";
import { Fragment } from "react/jsx-runtime";
import { QuizContainer } from "src/components/QuizContainer/QuizContainer";

export const QuizPage = () => {
  const quizId = useParams();

  return (
    <Fragment>
      <QuizContainer />
    </Fragment>
  );
};
