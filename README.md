# Java Git Implementation

A Java implementation of Git's core functionality, inspired by the "Write yourself a Git!" tutorial.
This project demonstrates understanding of Git's internal data structures and mechanisms by
recreating them from scratch.

## Project Overview

This project aims to implement a subset of Git's functionality in Java, focusing on the core
components that make Git work. By building Git from the ground up, this project demonstrates deep
understanding of:

- Git's object model and content-addressed storage
- Reference management and name resolution
- Version control fundamentals
- File system operations
- Hash functions and data integrity

## Implemented Features

- Core Git Objects
    - Blob Objects: Store file contents with proper serialization/deserialization
    - Commit Objects: Store commit metadata using key-value list with message (KVLM) format
    - Tag Objects: Store annotated tag data with proper metadata
    - Repository Management: Functions to create, find, and manipulate Git repositories
- References System
    - Simple references (branches, tags)
    - Object name resolution

## Commands

- `init`: Create a new, empty Git repository
- `cat-file`: Display contents of Git objects
- `hash-object`: Create Git objects from files
- `log`: Visualize commit history using Graphviz format
- `show-ref`: List all references in the repository
- `tag`: Create and list tags (both lightweight and annotated)

## Utilities

- Object Storage: Read and write Git objects with proper zlib compression
- SHA-1 Hashing: Calculate and verify object hashes
- KVLM Parsing: Parse and serialize Git's key-value list with message format
- Reference Management: Create, resolve, and list Git references
- Object Resolution: Resolve various types of object names (HEAD, branches, tags, hashes)

## Technical Implementation Details

### Git Object Model

The implementation models Git's object types as a class hierarchy:

```
GitObject (abstract)
├── GitBlob
├── GitCommit
├── GitTag
└── GitTree
```

## Key Components

- `GitRepository`: Manages repository paths and configuration
- `GitObjectUtil`: Handles object reading, writing, and hashing
- `GitCommit`: Parses and serializes commit data with KVLM format
- `GitTag`: Implements annotated tags with proper metadata
- `RefUtils`: Manages Git references (branches, tags)
- `LibWyag`: Main class with command implementations

## Interesting Technical Challenges

- KVLM Parsing: Implemented a robust parser for Git's commit format, handling multi-line values and
  continuation lines.
- Object Storage: Implemented proper object serialization with prefixes and zlib compression.
- Commit Graph Traversal: Created recursive traversal of the commit history for visualization.
- Reference Resolution: Implemented a system to resolve various types of Git references to their
  corresponding SHA-1 hashes.
- Object Name Resolution: Added support for resolving abbreviated hashes, symbolic names, and
  special references like HEAD.
- Tag Creation: Implemented both lightweight and annotated tags with proper formatting for tagger
  information.
