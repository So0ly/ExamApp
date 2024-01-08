import { createSlice } from '@reduxjs/toolkit';

const initialState = {
    studentList: [],
    uploadedQuestionList: [],
    selectedQuestionList: [],
    currentExam: '',
    currentStudent: null,
    questionTime: 40,
};

export const examSlice = createSlice({
    name: 'exam',
    initialState,
    reducers: {
        setStudentList: (state, action) => {
            state.studentList = action.payload;
        },
        setQuestionList: (state, action) => {
            state.uploadedQuestionList = action.payload;
        },
        setCurrentExam: (state, action) => {
          state.currentExam = action.payload;
        },
        setCurrentStudent: (state, action) => {
            state.currentStudent = action.payload;
        },
        setSelectedQuestionList: (state, action) => {
            state.selectedQuestionList = action.payload;
        },
        setQuestionTime: (state, action) => {
            state.questionTime = action.payload;
        },
        clearData: () => initialState,
        finishExam: (state, action) => {
            state.studentList = state.studentList.filter(student => student.index !== state.currentStudent.index);
            state.currentStudent = null;
        },
    },
});

export const { setStudentList, setQuestionList, setSelectedQuestionList, setCurrentStudent, setCurrentExam, finishExam, clearData, setQuestionTime } = examSlice.actions;

export const {reducer} = examSlice;
