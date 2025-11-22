import { useEffect } from "react";
import { signOut } from "src/model/authentications/authentications";
import { useAppDispatch } from "src/redux";
import { clearAuthentication } from "src/redux/authentication/authenticationSlice";
import { CardContainer } from "../CardContainer/CardContainer";
import { Container } from "../Container/Container";
import { LoaderSpinner } from "../LoaderSpinner/LoaderSpinner";
import * as styles from "./styles.module.scss";

export const SignOutContainer = () => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    performSignOut(() => {
      dispatch(clearAuthentication());
    });
  }, []);

  return (
    <Container>
      <CardContainer className={styles.rootContainer}>
        <LoaderSpinner />
      </CardContainer>
    </Container>
  );
};

async function performSignOut(onSignOutComplete: () => void) {
  try {
    await signOut();
    onSignOutComplete();
  } catch (e) {
    console.error(`Sign out request failed due to: ${e}`);
    onSignOutComplete();
  }
}
