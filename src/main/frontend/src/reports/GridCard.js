import React, {forwardRef} from 'react';
import { Card, CardActionArea, Typography, Grid, ButtonBase } from '@mui/material';
import CloudUploadIcon from "@mui/icons-material/CloudUpload";

export const GridCard = forwardRef(({ onClick }, ref) => {
    return (
        <Grid item xs={12} sm={6}>
            <CardActionArea sx={{ width: '100%', height: '100%'}} onClick={onClick} ref={ref}>
                <Card sx={{
                    p: 2,
                    '&:hover': {
                        backgroundColor: 'action.hover',
                        cursor: 'pointer'
                    },
                    height: '100%',
                    justifyContent:'center'
                }} align="center" >
                        <CloudUploadIcon fontSize="large" />
                        <Typography variant="body1" gutterBottom>
                            Importuj CSV
                        </Typography>
                </Card>
            </CardActionArea>
        </Grid>
    );
});
