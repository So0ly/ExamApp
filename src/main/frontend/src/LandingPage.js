import {useEffect} from "react";
import {useNavigate} from "react-router-dom";
import Cookies from "js-cookie";

export const LandingPage = () => {
    const navigate = useNavigate();

    useEffect(() => {
        const isLoggedIn = Cookies.get('jwt');

        if (isLoggedIn) {
            navigate('/reports');
        } else {
            navigate('/login');
        }
    }, [navigate]);
}