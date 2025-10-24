import { createAsyncThunk, createSlice, PayloadAction } from "@reduxjs/toolkit";
import { RootState } from "..";

interface CounterState {
  value: number;
  status: "idle" | "pending" | "fulfilled" | "rejected";
}

const initialState = {
  value: 0,
  status: "idle",
} as CounterState;

let DUMMY_COUNTER = 0;

export const incrementAsync = createAsyncThunk<number, number>(
  "counter/incrementAsync",
  async (amount, thunkAPI) => {
    DUMMY_COUNTER++;
    await new Promise((resolve, reject) =>
      setTimeout(() => {
        if (DUMMY_COUNTER % 3 === 0) {
          reject();
        } else {
          resolve(true);
        }
      }, 3000)
    );

    if (DUMMY_COUNTER % 5 === 0) {
      return thunkAPI.rejectWithValue("Cannot increment at multiples of 5");
    }

    return amount;
  }
);

export const counterSlice = createSlice({
  name: "counter",
  initialState,
  reducers: {
    increment: (state) => {
      state.value += 1;
    },
    decrement: (state) => {
      state.value -= 1;
    },
    incrementByAmount: (state, action: PayloadAction<number>) => {
      state.value += action.payload;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(incrementAsync.pending, (state) => {
        state.status = "pending";
      })
      .addCase(
        incrementAsync.fulfilled,
        (state, action: PayloadAction<number>) => {
          state.status = "fulfilled";
          state.value += action.payload;
        }
      )
      .addCase(incrementAsync.rejected, (state, action) => {
        state.status = "rejected";
      });
  },
});

export const { increment, decrement, incrementByAmount } = counterSlice.actions;

export const selectCount = (state: RootState) => state.counter.value;

export default counterSlice.reducer;
