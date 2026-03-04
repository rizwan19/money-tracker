import {BrowserRouter, Route, Routes} from "react-router-dom";
import Home from "./pages/Home.jsx";
import Signup from "./pages/Signup.jsx";
import Login from "./pages/Login.jsx";
import Category from "./pages/Category.jsx";
import Income from "./pages/Income.jsx";
import Expense from "./pages/Expense.jsx";
import Filter from "./pages/Filter.jsx";
import { Toaster } from 'react-hot-toast';

const App = () => {
    return (

        <>
            <Toaster/>
            <BrowserRouter>
                <Routes>
                    <Route path="/dashboard" element={<Home/>}/>
                    <Route path="/signup" element={<Signup/>}/>
                    <Route path="/login" element={<Login/>}/>
                    <Route path="/category" element={<Category/>}/>
                    <Route path="/income" element={<Income/>}/>
                    <Route path="/expense" element={<Expense/>}/>
                    <Route path="/filter" element={<Filter/>}/>
                </Routes>
            </BrowserRouter>
        </>
    )
}

export default App;
