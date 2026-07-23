import React from 'react';
import {useDispatch, useSelector} from 'react-redux';
import {
    Alert,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    TextField
} from '@mui/material';
import {useForm} from '../useForm';
import {closeChangePassword} from '../layout/redux';
import {examinerApi} from './';

export const ChangePasswordDialog = () => {
    const {values, invalid, isValid, error, setError, clearForm, onChange} = useForm({
        initialValues: {currentPassword: '', newPassword: ''}
    });
    const dispatch = useDispatch();
    const changePasswordOpen = useSelector(state => state.layout.changePasswordOpen);
    const close = () => dispatch(closeChangePassword());
    const [changePassword] = examinerApi.endpoints.changePassword.useMutation();
    const canSave = isValid && Boolean(values.currentPassword) && Boolean(values.newPassword);
    const save = () => {
        changePassword(values).then(({error}) => {
            if (!Boolean(error)) {
                clearForm();
                close();
            } else if (error?.status === 409) {
                setError('Aktualne hasło jest niepoprawne');
            } else {
                setError('Error');
            }
        });
    };
    return (
        <Dialog open={changePasswordOpen} onClose={close}>
            <DialogTitle>Zmień hasło</DialogTitle>
            <DialogContent>
                {Boolean(error) && <Alert severity='error'>{error}</Alert>}
                <TextField type='password' fullWidth margin='dense' variant='standard' label='Aktualne hasło'
                           name='currentPassword' value={values.currentPassword} onChange={onChange}
                           required error={Boolean(invalid.currentPassword)}
                           autoFocus
                />
                <TextField type='password' fullWidth margin='dense' variant='standard' label='Nowe hasło'
                           name='newPassword' value={values.newPassword} onChange={onChange}
                           required error={Boolean(invalid.newPassword)}
                />
            </DialogContent>
            <DialogActions>
                <Button onClick={close}>Cancel</Button>
                <Button onClick={save} disabled={!canSave}>Save</Button>
            </DialogActions>
        </Dialog>
    );
}