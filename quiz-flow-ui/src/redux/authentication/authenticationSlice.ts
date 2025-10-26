import { createAsyncThunk, createSlice, PayloadAction } from "@reduxjs/toolkit";
import { RootState } from "..";
import { signIn } from "src/model/authentication/authentication";
import { ApiClientError } from "src/model/utils/ApiClientError";
import { SignInResult } from "src/model/authentication/SignInResult";

interface SignInStateType {
  accessTokenId: string;
  refreshTokenId: string;
  accessToken: string;
  authorizationScopes: string[];
  accessTokenExpirationIsoDate: string;
  refreshTokenExpirationIsoDate: string;
}

interface AuthenticationState {
  signInResult: SignInStateType | null;
  signInRequestStatus: "idle" | "pending" | "fulfilled" | "rejected";
  errorMessage: string;
}

const initialState = {
  signInResult: null,
  signInRequestStatus: "idle",
  errorMessage: "",
} as AuthenticationState;

const NAME = "authentications";

export const signInAsync = createAsyncThunk<
  SignInStateType,
  { username: string; password: string },
  { rejectValue: string }
>(`${NAME}/signInAsync`, async (data, thunkAPI) => {
  const { password, username } = data;
  try {
    const signInResult = await signIn(username, password);
    return mapSignInResultToSignInState(signInResult);
  } catch (e) {
    if (e instanceof ApiClientError) {
      let message = e.message;
      if (e.data["reason"]) {
        message = `${message}. ${e.data["reason"]}`;
      }
      thunkAPI.dispatch(clearErrorMessage({ message: message }));
      return thunkAPI.rejectWithValue(message);
    }
    console.error("Cannot sign in due to unexpected error: " + e, e);
    return thunkAPI.rejectWithValue("Cannot sign in due to unexpected error");
  }
});

export const clearErrorMessage = createAsyncThunk<
  { resultMessage: string },
  { message: string }
>(`${NAME}/clearErrorMessage`, async (data, thunkAPI) => {
  const { message } = data;
  await new Promise<string>((resolve) => {
    setTimeout(() => {
      resolve(message);
    }, 3000);
  });
  return {
    resultMessage: message,
  };
});

export const authenticationSlice = createSlice({
  name: NAME,
  initialState,
  reducers: {
    setSignInResult: (state, action: PayloadAction<SignInStateType>) => {
      state.signInResult = action.payload;
    },
    clearAuthentication: (state) => {
      state.signInResult = null;
    },
  },
  extraReducers: (builder) => {
    builder.addCase(
      clearErrorMessage.fulfilled,
      (state, action: PayloadAction<{ resultMessage: string }>) => {
        if (action.payload.resultMessage === state.errorMessage) {
          state.errorMessage = "";
        }
      }
    );

    builder
      .addCase(signInAsync.pending, (state) => {
        state.signInRequestStatus = "pending";
      })
      .addCase(
        signInAsync.fulfilled,
        (state, action: PayloadAction<SignInStateType>) => {
          state.signInRequestStatus = "fulfilled";
          state.signInResult = action.payload;
        }
      )
      .addCase(signInAsync.rejected, (state, action) => {
        state.signInRequestStatus = "rejected";
        if (action.payload) {
          state.errorMessage = action.payload;
        }
      });
  },
});

export const { setSignInResult, clearAuthentication } =
  authenticationSlice.actions;

export const selectIsAuthenticated = (state: RootState) =>
  state.authentication.signInResult !== null;
export const selectIsSignInRequestOngoing = (state: RootState) =>
  state.authentication.signInRequestStatus === "pending";
export const selectErrorMessage = (state: RootState) =>
  state.authentication.errorMessage;

export default authenticationSlice.reducer;

export function mapSignInResultToSignInState(
  signInResult: SignInResult
): SignInStateType {
  return {
    accessTokenId: signInResult.accessTokenId,
    refreshTokenId: signInResult.refreshTokenId,
    accessToken: signInResult.accessToken,
    authorizationScopes: signInResult.authorizationScopes,
    accessTokenExpirationIsoDate:
      signInResult.accessTokenExpirationDate.toISOString(),
    refreshTokenExpirationIsoDate:
      signInResult.refreshTokenExpirationDate.toISOString(),
  };
}
