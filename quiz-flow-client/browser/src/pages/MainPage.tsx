import { CardContainer } from "src/components/CardContainer/CardContainer";
import { Container } from "src/components/Container/Container";
import { LatestQuestionSets } from "src/components/LatestQuestionSets/LatestQuestionSets";

export const MainPage = () => {
  return (
    <Container>
      <CardContainer>
        <LatestQuestionSets />
      </CardContainer>
    </Container>
  );
};
