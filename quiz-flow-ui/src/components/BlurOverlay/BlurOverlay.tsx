import { PropsWithChildren } from "react";
import * as styles from "./styles.module.scss";

export const BlurOverlay = (props: PropsWithChildren) => {
  return <div className={styles.overlay}>{props.children}</div>;
};
