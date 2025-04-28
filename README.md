# Java Git Implementation

A Java implementation of Git's core functionality, inspired by the "Write yourself a Git!" tutorial.
This project demonstrates understanding of Git's internal data structures and mechanisms by
recreating them from scratch.

## Project Overview

This project aims to implement a subset of Git's functionality in Java, focusing on the core
components that make Git work. By building Git from the ground up, this project demonstrates deep
understanding of:

- Git's object model and content-addressed storage
- Version control fundamentals
- File system operations
- Hash functions and data integrity

## Implemented Features

- Core Git Objects

    - Blob Objects: Store file contents with proper serialization/deserialization
    - Commit Objects: Store commit metadata using key-value list with message (KVLM) format
    - Repository Management: Functions to create, find, and manipulate Git repositories

## Commands

- init: Create a new, empty Git repository
- cat-file: Display contents of Git objects
- hash-object: Create Git objects from files
- log: Visualize commit history using Graphviz format

## Utilities

- Object Storage: Read and write Git objects with proper zlib compression
- SHA-1 Hashing: Calculate and verify object hashes
- KVLM Parsing: Parse and serialize Git's key-value list with message format

## Technical Implementation Details

### Git Object Model

The implementation models Git's object types as a class hierarchy:

```
GitObject (abstract)
├── GitBlob
├── GitCommit
├── GitTag
└── GitTree (in progress)
```

## Key Components

- GitRepository: Manages repository paths and configuration
- GitObjectUtil: Handles object reading, writing, and hashing
- GitCommit: Parses and serializes commit data with KVLM format
- LibWyag: Main class with command implementations

## Interesting Technical Challenges

- KVLM Parsing: Implemented a robust parser for Git's commit format, handling multi-line values and
  continuation lines.
- Object Storage: Implemented proper object serialization with prefixes and zlib compression.
- Commit Graph Traversal: Created recursive traversal of the commit history for visualization.
