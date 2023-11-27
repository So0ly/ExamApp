import React from 'react';
import {Container, IconButton, Paper, Table, TableBody, TableCell, TableHead, TableRow, Typography} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import {api} from './api';
import {Layout} from '../layout';

export const Examiners = () => {
  const {data: allExaminers} = api.endpoints.getExaminers.useQuery(undefined, {pollingInterval: 10000});
  const {data: self} = api.endpoints.getSelf.useQuery();
  const [deleteExaminer] = api.endpoints.deleteExaminer.useMutation();
  return <Layout>
    <Container sx={{mt: theme => theme.spacing(2)}}>
      <Paper sx={{p: 2}}>
        <Typography component="h2" variant="h6" color="primary" gutterBottom>
          Egzaminatorzy
        </Typography>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Mail</TableCell>
              <TableCell>Tytuły</TableCell>
              <TableCell>Imię</TableCell>
              <TableCell>Nazwisko</TableCell>
              <TableCell>Uprawnienia</TableCell>
              <TableCell/>
            </TableRow>
          </TableHead>
          <TableBody>
            {allExaminers && allExaminers.map(examiner =>
              <TableRow key={examiner.id}>
                <TableCell>{examiner.mail}</TableCell>
                <TableCell>{examiner.titles}</TableCell>
                <TableCell>{examiner.firstName}</TableCell>
                <TableCell>{examiner.lastName}</TableCell>
                <TableCell>{examiner.roles.join(', ')}</TableCell>
                <TableCell align='right'>
                  <IconButton
                    disabled={examiner.id === self?.id} onClick={() => deleteExaminer(examiner)}
                  >
                    <DeleteIcon/>
                  </IconButton>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Paper>
    </Container>
  </Layout>;
};