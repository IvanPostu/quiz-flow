import { CardContainer } from "../CardContainer/CardContainer";
import { Container } from "../Container/Container";
import { LoaderSpinner } from "../LoaderSpinner/LoaderSpinner";
import * as styles from "./styles.module.scss";

export const SignInContainer = () => {
  return (
    <Container>
      <CardContainer className={styles.rootContainer}>
        <div className={styles.overlay}>
          <LoaderSpinner />
        </div>
        <h2 className={styles.title}>Sign-In</h2>
        <div className={styles.element}>
          <label>
            <span>Username:</span>
            <input type="text" required />
          </label>
        </div>
        <div className={styles.element}>
          <label>
            <span>Password:</span>
            <input type="password" required />
          </label>
        </div>
        <div className={styles.element}>
          <span style={{ color: "var(--accent-red)" }}>
            *Something went wrong
          </span>
        </div>
        <div className={styles.element}>
          <button>Sign In</button>
        </div>
      </CardContainer>
    </Container>
  );
};
