import { PropsWithChildren } from "react";
import * as styles from "./styles.module.scss";

type CardContainerPropsType = PropsWithChildren<{
  className?: string;
}>;

export const CardContainer = (props: CardContainerPropsType) => {
  return (
    <div className={`${props.className || ""} ${styles.rootContainer}`}>
      {props.children}
    </div>
  );
};
