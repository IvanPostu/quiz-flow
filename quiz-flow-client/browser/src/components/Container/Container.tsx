import { PropsWithChildren } from "react";
import * as styles from "./styles.module.scss";

export const Container = (props: PropsWithChildren) => {
  return <div className={styles.containerRoot}>{props.children}</div>;
};
