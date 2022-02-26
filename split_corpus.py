with open('./corpus/cran.all.1400') as f:
    lines = f.read()
    split_file = lines.split('.I ')

    for i in range(1,1401):
        filename = "./split_corpus/" + str(i)
        new_file = open(filename, 'w')
        new_file.write(split_file[i])
        new_file.close()
