import React from "react";
import * as styles from "./styles.module.scss";

interface KeyValueContainerProps {
  data: Record<string, string>;
  keyFlexBasis?: string;
}

export const KeyValueContainer: React.FC<KeyValueContainerProps> = ({
  data,
  keyFlexBasis,
}) => {
  const keyStyle = {} as React.CSSProperties;
  if (keyFlexBasis) {
    keyStyle.flexBasis = keyFlexBasis;
  }

  return (
    <div className={styles.keyValueContainer}>
      {Object.entries(data).map(([key, value]) => (
        <div className={styles.keyValueRow} key={key}>
          <div style={keyStyle} className={styles.key}>
            {key}
          </div>
          <div className={styles.value}>{value}</div>
        </div>
      ))}
    </div>
  );
};

export function calculateData(signInResult: Record<string, any>) {
  const result = Object.entries(signInResult || {}).reduce<
    Record<string, string>
  >((acc, [key, value]) => {
    acc[key] = "" + value;
    return acc;
  }, {});

  return result;
}
