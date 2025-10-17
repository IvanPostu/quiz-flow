import { CSSProperties, PropsWithChildren } from "react";
import * as styles from "./styles.module.scss";

type CardContainerPropsType = PropsWithChildren<{
  className?: string;
  style?: CSSProperties;
}>;

export const CardContainer = (props: CardContainerPropsType) => {
  return (
    <div
      style={props.style}
      className={`${props.className || ""} ${styles.rootContainer}`}
    >
      {props.children}
    </div>
  );
};
