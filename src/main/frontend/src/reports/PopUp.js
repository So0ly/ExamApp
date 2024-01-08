import {Box, Modal, Typography} from "@mui/material";
import React from "react";

export const PopUp = ({ title, popUpMsg, open, close,children }) => {

    return (
<Modal
    open={open}
    onClose={close}
>
    <Box sx={{
        position: 'absolute',
        top: '50%',
        left: '50%',
        transform: 'translate(-50%, -50%)',
        width: 400,
        bgcolor: 'background.paper',
        border: '2px solid #000',
        boxShadow: 24,
        p: 4,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center'
    }}>
        <Typography variant="h6" component="h2">
            {title}
        </Typography>
        <Typography sx={{ mt: 2 }}>
            {popUpMsg}
        </Typography>
        {children}
    </Box>
</Modal>
)};