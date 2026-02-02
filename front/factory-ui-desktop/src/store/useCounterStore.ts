import { create, type StateCreator } from "zustand";

interface CounterState {
  count: number;

  increment: () => void;
  decrement: () => void;
  reset: () => void;
}

const storeCreator: StateCreator<CounterState> = (set) => ({
  count: 0,

  increment: () => set((state) => ({ count: state.count + 1 })),
  decrement: () => set((state) => ({ count: state.count - 1 })),
  reset: () => set({ count: 0 }),
});

const useCounterStore = create<CounterState>(storeCreator);

export default useCounterStore;
