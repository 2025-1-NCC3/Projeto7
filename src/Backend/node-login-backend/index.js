const express = require('express');
const sqlite3 = require('sqlite3').verbose();
const bcrypt = require('bcryptjs');
const bodyParser = require('body-parser');

// Criando o servidor
const app = express();
//const port = 3000;
const port = process.env.PORT || 3000;

// Usando o body-parser para lidar com JSON
app.use(bodyParser.json());

// Conectando ao banco de dados SQLite
const db = new sqlite3.Database('./users.db', (err) => {
  if (err) {
    console.error("Erro ao conectar ao banco de dados: " + err.message);
  } else {
    console.log("Conectado ao banco de dados SQLite.");
  }
});

// Criando a tabela de usuários (se ainda não existir)
db.run(`CREATE TABLE IF NOT EXISTS users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  email TEXT NOT NULL UNIQUE,
  password TEXT NOT NULL
)`);

// Rota para cadastro de usuário
app.post('/register', (req, res) => {
    const { email, password } = req.body;
  
    // Log para depuração
    console.log('Dados recebidos:', { email, password });
  
    // Verifica se todos os dados foram enviados
    if (!email || !password) {
      return res.status(400).json({ message: 'Usuário e senha são obrigatórios.' });
    }
  
    // Criptografar a senha usando bcrypt
    bcrypt.hash(password, 10, (err, hashedPassword) => {
      if (err) {
        console.error('Erro ao criptografar a senha:', err);
        return res.status(500).json({ message: 'Erro ao criptografar a senha.' });
      }
  
      console.log('Senha criptografada:', hashedPassword); // Adicionando o log da senha criptografada
  
      // Salvar no banco de dados
      db.run('INSERT INTO users (email, password) VALUES (?, ?)', [email, hashedPassword], function(err) {
        if (err) {
          if (err.message.includes('UNIQUE constraint failed')) {
            return res.status(400).json({ message: 'Usuário já cadastrado.' });
          }
          return res.status(500).json({ message: 'Erro ao cadastrar o usuário.' });
        }
        res.status(201).json({ message: 'Usuário cadastrado com sucesso.' });
      });
    });
  });
  

// Rota de login de usuário
app.post('/login', (req, res) => {
  console.log('Corpo recebido:', req.body);
  const { email, password } = req.body;

  if (!email || !password) {
    return res.status(400).json({ message: 'Usuário e senha são obrigatórios.' });
  }

  // Buscar o usuário no banco de dados
  db.get('SELECT * FROM users WHERE email = ?', [email], async (err, row) => {
    if (err || !row) {
      return res.status(400).json({ message: 'Usuário não encontrado.' });
    }

    // Comparar a senha fornecida com a senha criptografada
    const isMatch = await bcrypt.compare(password, row.password);

    if (isMatch) {
      res.status(200).json({ message: 'Login bem-sucedido!' });
    } else {
      res.status(400).json({ message: 'Senha incorreta.' });
    }
  });
});

// Iniciar o servidor
app.listen(port, () => {
  console.log(`Servidor rodando em http://localhost:${port}`);
});
