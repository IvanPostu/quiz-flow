import { CardContainer } from "../CardContainer/CardContainer";
import { Container } from "../Container/Container";
import { LoaderSpinner } from "../LoaderSpinner/LoaderSpinner";
import * as styles from "./styles.module.scss";

export const SignOutContainer = () => {
  return (
    <Container>
      <CardContainer className={styles.rootContainer}>
        <LoaderSpinner />
      </CardContainer>
    </Container>
  );
};
