import { CardContainer } from "src/components/CardContainer/CardContainer";
import { Container } from "src/components/Container/Container";
import { LatestQuestionSets } from "src/components/LatestQuestionSets/LatestQuestionSets";
import { MainPageContainer } from "src/components/MainPageContainer/MainPageContainer";

export const MainPage = () => {
  return (
    <Container>
      <MainPageContainer />
      <CardContainer>
        <LatestQuestionSets />
        <LatestQuestionSets />
        <LatestQuestionSets />
        <LatestQuestionSets />
        <LatestQuestionSets />
        <LatestQuestionSets />
      </CardContainer>
    </Container>
  );
};
