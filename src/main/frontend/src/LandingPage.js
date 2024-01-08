import {useEffect} from "react";
import {useNavigate} from "react-router-dom";

export const LandingPage = () => {
    const navigate = useNavigate();

    useEffect(() => {
        const isLoggedIn = sessionStorage.getItem('jwt');

        if (isLoggedIn) {
            navigate('/reports');
        } else {
            navigate('/login');
        }
    }, [navigate]);
}