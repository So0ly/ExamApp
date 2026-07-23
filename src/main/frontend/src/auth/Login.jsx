import React, {useEffect} from 'react';
import {redirect, useNavigate} from 'react-router-dom';
import {Avatar, Box, Button, Container, Snackbar, TextField, Typography} from '@mui/material';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import {useDispatch} from 'react-redux';
import {login} from './redux';
import {useForm} from '../useForm';
import Cookies from "js-cookie";

export const Login = () => {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const {values, isValid, error, setError, onChange} = useForm({
        initialValues: {mail: '', password: ''}
    });

    useEffect(() => {
        const jwt = Cookies.get('jwt');
        if (jwt) {
            navigate('/reports');
        }
    }, [navigate]);

    const sendLogin = () => {
        if (isValid) {
            dispatch(login({mail: values.mail, password: values.password}))
                .then(({meta, payload}) => {
                    if (meta.requestStatus === 'fulfilled') {
                        navigate('/landing');
                    } else if (payload?.status === 401) {
                        setError('Złe dane logowania');
                    } else {
                        setError('Error');
                    }
                });
        }
    };
    return (
        <Container maxWidth='xs'>
            <Box sx={{mt: theme => theme.spacing(8), display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
                <Avatar sx={{m: 1}}>
                    <LockOutlinedIcon />
                </Avatar>
                <Typography component='h1' variant='h5'>
                    Zaloguj się do aplikacji
                </Typography>
                <Box noValidate sx={{ mt: 1 }}>
                    <TextField margin='normal' required fullWidth autoFocus
                               label='Mail' name='mail' onChange={onChange} value={values.mail}
                    />
                    <TextField type='password' margin='normal' required fullWidth
                               label='Hasło' name='password' onChange={onChange} value={values.password}
                               onKeyDown={e => e.key === 'Enter' && sendLogin()}
                    />
                    <Button fullWidth variant='contained' onClick={sendLogin} sx={{ mt: 3, mb: 2 }}>
                        Zaloguj się
                    </Button>
                    <Button fullWidth variant='outlined' onClick={() => navigate('/register')} sx={{ mt: 3, mb: 2 }}>
                        Nie masz konta? Zarejestruj się
                    </Button>
                </Box>
            </Box>
            <Snackbar
                open={Boolean(error)} message={error}
                autoHideDuration={6000} onClose={() => setError(null)}
            />
        </Container>
    );
};