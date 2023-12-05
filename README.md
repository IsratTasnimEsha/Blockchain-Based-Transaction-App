# Blockchain Transaction System

<br>


  <img src="![C](https://github.com/IsratTasnimEsha/Blockchain-Based-Transaction-App/assets/88322977/84fbc5e4-958a-4a0e-bd45-53143f068516)" alt="Logo" width="12%">


<p align="center">
https://github.com/IsratTasnimEsha/Blockchain-Based-Transaction-App/assets/88322977/1fc4cc1a-5426-428e-a15e-3cb31e655ea6
</p>


Welcome to the Blockchain Transaction System project! This project implements a custom transaction and blockchain system, ensuring secure and transparent financial transactions. Users can sign up, perform transactions, mine blocks, and manage their accounts in a decentralized environment.

## Table of Contents
- [Introduction](#introduction)
- [Features](#features)
- [System Architecture](#system-architecture)
- [Activities](#activities)
- [Usage](#usage)
- [Validation](#validation)
- [Account Management](#account-management)
- [Security](#security)
- [Notification System](#notification-system)
- [Database Integrity](#database-integrity)
- [Contributing](#contributing)
- [License](#license)
- [Authors](#authors)

## Introduction
This project implements a custom blockchain-based transaction system. Users can sign up, perform secure transactions, and participate in the mining process to add new blocks to the blockchain. The system ensures data integrity and security through digital signatures and SHA-256 hash generation.

## Features
- **User Authentication:** Users can sign up and sign in securely with their email and password. A user is initialized with $100 and receives a private and public key pair.
- **Transaction Processing:** Users can initiate transactions with a receiver, and fees are deducted for miners. Digital signatures are generated for each transaction to ensure authenticity.
- **Mining:** Miners verify transactions and use the SHA-256 hash generator for mining. Successfully mined blocks are broadcasted to all users, triggering updates to their block queues.
- **Block Queue Management:** Users can review and accept transactions in the block queue. Accepted blocks are added to the user's temporary blockchain.
- **Blockchain Updates:** After successfully adding a block to the main blockchain, user balances are updated, and notifications are broadcasted to all users.
- **Security Measures:** Four random user accounts are regularly checked to validate the database's integrity. Attempts to manipulate the database are promptly identified.

## System Architecture
The system follows a decentralized architecture, with users participating in transaction processing and mining. The blockchain ensures a transparent and secure ledger of all transactions.

## Activities
- **Splash Screen:** Provides an initial interface for users.
- **Sign Up:** Users can create an account, generating private and public keys.
- **Sign In:** Securely sign in using email and password.
- **Add Transaction Fragment:** Initiate a transaction with fees and generate a digital signature.
- **Add to Block Fragment:** Miners verify transactions and prepare them for mining.
- **Mine Fragment:** Participate in the mining process using the SHA-256 hash generator.
- **Block Queue:** Review pending transactions in the block queue.
- **Block Queue Details:** Verify and accept transactions within a block.
- **Blockchain:** View the main blockchain and track transactions.
- **Blockchain Details:** Explore details of each block in the main blockchain.
- **Rejected Blocks:** Access information on rejected blocks.
- **Notification:** Receive updates and notifications on blockchain activities.
- **Account:** Manage user account information, change details, and reset passwords.
- **Forgot Password:** Recover forgotten passwords securely.

![1](https://github.com/IsratTasnimEsha/Blockchain-Based-Transaction-App/assets/88322977/0a977ef0-5c6d-4e0c-8fbf-eab9136b96ea)

![2](https://github.com/IsratTasnimEsha/Blockchain-Based-Transaction-App/assets/88322977/8414a708-d63b-41c4-984a-83201c333419)

![3](https://github.com/IsratTasnimEsha/Blockchain-Based-Transaction-App/assets/88322977/4d28ff36-bad8-4d4e-8c0f-a3f6df85a1f8)

![4](https://github.com/IsratTasnimEsha/Blockchain-Based-Transaction-App/assets/88322977/a9ad05e1-1b6a-4ede-ab2e-7cc188013ab2)

## Usage
The system is designed to be user-friendly. Follow the prompts and interfaces for each activity to perform transactions, mine blocks, and manage your account securely.

## Validation
To maintain database integrity, four random user accounts are regularly checked. Any attempts to manipulate the database will be caught and addressed promptly.

## Account Management
Users can change account information (excluding email and phone) and reset passwords securely within the account activity.

## Security
The project implements secure authentication, digital signatures, and blockchain encryption to ensure user data integrity and prevent unauthorized access.

## Notification System
Stay informed about blockchain activities with the built-in notification system. Updates are broadcasted to all users for transparency.

## Database Integrity
The system regularly validates the database by checking four random user accounts. Any discrepancies are detected and addressed promptly.

## Contributing
Contributions are welcome! Feel free to submit issues, feature requests, or pull requests. Please adhere to the project's coding standards and guidelines.

## License
This project is licensed under the MIT License.

Feel free to customize this README file further based on your project's specific details and requirements.

## Authors

- Israt Tasnim Esha
- Hasibul Hasan Hasib

*Department of Computer Science and Engineering*  
*Khulna University of Engineering and Technology*
