import { Container } from "../Container/Container";
import { CardContainer } from "../CardContainer/CardContainer";
import * as styles from "./styles.module.scss";
import { Link } from "react-router-dom";

export const SignUpContainer = () => {
  return (
    <Container>
      <CardContainer className={styles.rootContainer}>
        <h2 className={styles.title}>Sign-Up</h2>
        <div className={styles.element}>
          <b>
            The only way to create an account is by contacting the
            administrator.
          </b>
        </div>
        <div className={styles.element}>
          <Link to={"/sign-in"}>Sign In</Link>
        </div>
      </CardContainer>
    </Container>
  );
};
