import * as styles from "./_globalVariables.module.scss";

export const globalStyleVariables = [
  "gray100",
  "gray200",
  "gray300",
  "gray400",
  "gray500",
  "whiteBlue100",
  "whiteBlue200",
  "whiteBlue300",
  "whiteBlue400",
  "whiteBlue500",
  "blue100",
  "blue200",
  "blue300",
  "blue400",
  "blue500",
  "green100",
  "green200",
  "green300",
  "green400",
  "green500",
  "red100",
  "red200",
  "red300",
  "red400",
  "red500",
  "yellow100",
  "yellow200",
  "yellow300",
  "yellow400",
  "yellow500",
  "white100",
  "white200",
  "white300",
  "dark100",
  "dark200",
  "dark300",
  "greyBorder",
  "greyBorderBoxShadow",
] as const;

export type GlobalStyleVariableType = (typeof globalStyleVariables)[number];

const globals = globalStyleVariables.reduce((acc, key) => {
  acc[key] = styles[key];
  return acc;
}, {} as Record<GlobalStyleVariableType, string>);

export default globals;
